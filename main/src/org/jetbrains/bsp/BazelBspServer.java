package org.jetbrains.bsp;

import ch.epfl.scala.bsp4j.*;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.devtools.build.lib.analysis.AnalysisProtos;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BazelBspServer implements BuildServer, ScalaBuildServer, JavaBuildServer {

    private final String bazel;
    private final Path home;
    private String BES_BACKEND = "--bes_backend=grpc://localhost:";
    private final String PUBLISH_ALL_ACTIONS = "--build_event_publish_all_actions";
    public static final ImmutableSet<String> KNOWN_SOURCE_ROOTS =
            ImmutableSet.of("java", "scala", "kotlin", "javatests", "src", "testsrc");
    private static final String SCALAC = "Scalac";
    private static final String KOTLINC = "KotlinCompile";
    private static final String JAVAC = "Javac";

    private final Map<BuildTargetIdentifier, List<SourceItem>> targetsToSources = new HashMap<>();

    private final CompletableFuture<Void> isInitialized = new CompletableFuture<>();
    private final CompletableFuture<Void> isFinished = new CompletableFuture<>();

    public BepServer bepServer = null;
    private String execRoot = null;
    private String workspaceRoot = null;
    private String binRoot = null;
    private ScalaBuildTarget scalacClasspath = null;
    private BuildClient buildClient;
    private final List<String> fileExtensions = Lists.newArrayList(
            ".scala",
            ".java",
            ".kt",
            ".kts",
            ".sh",
            ".bzl",
            ".py",
            ".js",
            ".c",
            ".h",
            ".cpp",
            ".hpp");

    private final PathMatcher sourceRootPattern = FileSystems.getDefault().getPathMatcher(
            "glob:**/{main,test,tests,src,3rdparty,3rd_party,thirdparty,third_party}/{resources,scala,kotlin,java,jvm,proto,python,protobuf,py}"
    );
    private final PathMatcher defaultTestRootPattern = FileSystems.getDefault().getPathMatcher(
            "glob:**/{test,tests}"
    );

    public BazelBspServer(String pathToBazel, Path home) {
        this.bazel = pathToBazel;
        this.home = home;
    }

    public void setBackendPort(int port) {
        this.BES_BACKEND += port;
    }

    private boolean isInitialized() {
        try {
            isInitialized.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
        return true;
    }

    private boolean isFinished() {
        return isFinished.isDone();
    }

    private <T> CompletableFuture<T> completeExceptionally(ResponseError error) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(new ResponseErrorException(error));
        return future;
    }

    private <T> CompletableFuture<T> handleBuildInitialize(Supplier<Either<ResponseError, T>> request) {
        if (isFinished())
            return completeExceptionally(new ResponseError(ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
        return getValue(request);
    }

    private <T> CompletableFuture<T> handleBuildShutdown(Supplier<Either<ResponseError, T>> request) {
        if (!isInitialized())
            return completeExceptionally(new ResponseError(ResponseErrorCode.serverErrorEnd, "Server has not been initialized yet!", false));
        return getValue(request);
    }

    private <T> CompletableFuture<T> executeCommand(Supplier<Either<ResponseError, T>> request) {
        if (!isInitialized())
            return completeExceptionally(new ResponseError(ResponseErrorCode.serverNotInitialized, "Server has not been initialized yet!", false));
        if (isFinished())
            return completeExceptionally(new ResponseError(ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));

        return getValue(request);
    }

    private <T> CompletableFuture<T> getValue(Supplier<Either<ResponseError, T>> request) {
        CompletableFuture<Either<ResponseError, T>> execution = CompletableFuture.supplyAsync(request);
        Either<ResponseError, T> either;
        try {
            either = execution.get();
        } catch (CompletionException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return completeExceptionally(new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
        }

        if (either.isLeft())
            return completeExceptionally(either.getLeft());
        else
            return CompletableFuture.completedFuture(either.getRight());
    }

    @Override
    public CompletableFuture<InitializeBuildResult> buildInitialize(
            InitializeBuildParams initializeBuildParams) {
        return handleBuildInitialize(() -> {
            final List<String> supportedLanguages = Lists.newArrayList("scala", "java", "kotlin");
            BuildServerCapabilities capabilities = new BuildServerCapabilities();
            capabilities.setCompileProvider(new CompileProvider(supportedLanguages));
            capabilities.setRunProvider(new RunProvider(supportedLanguages));
            capabilities.setTestProvider(new TestProvider(supportedLanguages));
            capabilities.setDependencySourcesProvider(true);
            capabilities.setInverseSourcesProvider(true);
            capabilities.setResourcesProvider(true);
            checkBazelInstallation();
            return Either.forRight(new InitializeBuildResult(
                    Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities));
        });
    }

    private void checkBazelInstallation() {
        try {
            Process process = startProcess();
            parseProcess(process);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBuildInitialized() {
        isInitialized.complete(null);
    }

    @Override
    public CompletableFuture<Object> buildShutdown() {
        return handleBuildShutdown(() -> {
            isFinished.complete(null);
            return Either.forRight(new Object());
        });
    }

    @Override
    public void onBuildExit() {
        try {
            isFinished.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.exit(1);
        }

        System.exit(0);
    }

    @Override
    public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
        // TODO(illicitonion): Parameterise this to allow importing a subset of //...
        return executeCommand(() -> {
            try {
                Build.QueryResult queryResult = Build.QueryResult.parseFrom(
                        runBazelStream("query", "--output=proto", "--nohost_deps",
                                "--noimplicit_deps", "kind(binary, //...) union kind(library, //...) union kind(test, //...)"));
                List<BuildTarget> targets = queryResult.getTargetList().stream()
                        .map(Build.Target::getRule)
                        .filter(rule -> !rule.getRuleClass().equals("filegroup"))
                        .map(this::getBuildTarget).collect(Collectors.toList());
                return Either.forRight(new WorkspaceBuildTargetsResult(targets));
            } catch (IOException e) {
                return Either.forLeft(new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
            }
        });
    }

    private BuildTarget getBuildTarget(Build.Rule rule) {
        String name = rule.getName();
        System.out.println("Getting targets for rule: " + name);
        List<BuildTargetIdentifier> deps = rule.getAttributeList()
                .stream().filter(attribute -> attribute.getName().equals("deps"))
                .flatMap(srcDeps -> srcDeps.getStringListValueList().stream())
                .map(BuildTargetIdentifier::new)
                .collect(Collectors.toList());
        BuildTargetIdentifier label = new BuildTargetIdentifier(name);

        List<SourceItem> sources = getSourceItems(rule, label);

        Set<String> extensions = new TreeSet<>();

        for (SourceItem source : sources) {
            if (source.getUri().endsWith(".scala")) {
                extensions.add("scala");
            } else if (source.getUri().endsWith(".java")) {
                extensions.add("java");
            } else if (source.getUri().endsWith(".kt")) {
                extensions.add("kotlin");
                extensions.add("java"); //TODO(andrefmrocha): Remove this when kotlin is natively support
            }
        }

        String ruleClass = rule.getRuleClass();
        BuildTarget target =
                new BuildTarget(
                        label,
                        new ArrayList<>(),
                        new ArrayList<>(extensions),
                        deps,
                        new BuildTargetCapabilities(true, ruleClass.endsWith("_test"), ruleClass.endsWith("_binary")));
        target.setBaseDirectory(Uri.packageDirFromLabel(label.getUri(), getWorkspaceRoot()).toString());
        target.setDisplayName(label.getUri());
        if (extensions.contains("scala")) {
            getScalaBuildTarget().ifPresent((buildTarget) -> {
                target.setDataKind(BuildTargetDataKind.SCALA);
                target.setTags(Lists.newArrayList(getRuleType(rule)));
                target.setData(buildTarget);
            });
        } else if (extensions.contains("java") || extensions.contains("kotlin")) {
            target.setDataKind(BuildTargetDataKind.JVM);
            target.setTags(Lists.newArrayList(getRuleType(rule)));
            target.setData(getJVMBuildTarget());
        }
        return target;
    }

    private String getRuleType(Build.Rule rule) {
        String ruleClass = rule.getRuleClass();
        if (ruleClass.contains("library"))
            return BuildTargetTag.LIBRARY;
        if (ruleClass.contains("binary"))
            return BuildTargetTag.APPLICATION;
        if (ruleClass.contains("test"))
            return BuildTargetTag.TEST;
        return BuildTargetTag.NO_IDE;
    }

    private List<SourceItem> getSourceItems(Build.Rule rule, BuildTargetIdentifier label) {
        List<SourceItem> srcs = getSrcs(rule, false);
        srcs.addAll(getSrcs(rule, true));
        targetsToSources.put(label, srcs);
        return srcs;
    }

    private List<SourceItem> getSrcs(Build.Rule rule, boolean isGenerated) {
        String srcType = isGenerated ? "generated_srcs" : "srcs";
        return getSrcsPaths(rule, srcType).stream()
                .map(uri -> new SourceItem(uri.toString(), SourceItemKind.FILE, isGenerated))
                .collect(Collectors.toList());
    }

    private List<Uri> getSrcsPaths(Build.Rule rule, String srcType) {
        return rule.getAttributeList()
                .stream().filter(attribute -> attribute.getName().equals(srcType))
                .flatMap(srcsSrc -> srcsSrc.getStringListValueList().stream())
                .flatMap(dep -> {
                    if (isSourceFile(dep))
                        return Lists.newArrayList(Uri.fromFileLabel(dep, getWorkspaceRoot())).stream();

                    try {
                        Build.QueryResult queryResult = Build.QueryResult.parseFrom(
                                runBazelStream("query", "--output=proto", dep));
                        return queryResult.getTargetList().stream()
                                .map(Build.Target::getRule)
                                .flatMap(queryRule -> getSrcsPaths(queryRule, srcType).stream())
                                .collect(Collectors.toList()).stream();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isSourceFile(String dep) {
        return fileExtensions.stream().anyMatch(dep::endsWith) && !dep.startsWith("@");
    }

    private Optional<ScalaBuildTarget> getScalaBuildTarget() {
        if (scalacClasspath == null) {
            // Force-populate cache to avoid deadlock when looking up execRoot from BEP listener.
            getExecRoot();
            buildTargetsWithBep(
                    Lists.newArrayList(
                            new BuildTargetIdentifier("@io_bazel_rules_scala_scala_library//:io_bazel_rules_scala_scala_library"),
                            new BuildTargetIdentifier("@io_bazel_rules_scala_scala_reflect//:io_bazel_rules_scala_scala_reflect"),
                            new BuildTargetIdentifier("@io_bazel_rules_scala_scala_compiler//:io_bazel_rules_scala_scala_compiler")
                    ),
                    Lists.newArrayList("--aspects=@//.bazelbsp:aspects.bzl%scala_compiler_classpath_aspect", "--output_groups=scala_compiler_classpath_files")
            );
            List<String> classpath = bepServer.fetchScalacClasspath().stream().map(Uri::toString).collect(Collectors.toList());
            List<String> scalaVersions = classpath.stream().filter(uri -> uri.contains("scala-library")).collect(Collectors.toList());
            if (scalaVersions.size() != 1)
                return Optional.empty();
            String scalaVersion = scalaVersions.get(0).substring(scalaVersions.get(0).indexOf("scala-library-") + 14, scalaVersions.get(0).indexOf(".jar"));
            scalacClasspath = new ScalaBuildTarget("org.scala-lang",
                    scalaVersion, scalaVersion.substring(0, scalaVersion.lastIndexOf(".")), ScalaPlatform.JVM, classpath);
            scalacClasspath.setJvmBuildTarget(getJVMBuildTarget());
        }
        return Optional.of(scalacClasspath);
    }

    private JvmBuildTarget getJVMBuildTarget() {
        Uri javaHome = Uri.fromAbsolutePath(System.getProperty("java.home"));
        String javaVersion = getJavaVersion();
        return new JvmBuildTarget(
                javaHome.toString(),
                javaVersion
        );
    }

    private String getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(0, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return version;
    }

    private List<String> runBazelLines(String... args) {
        List<String> lines =
                Splitter.on("\n").omitEmptyStrings().splitToList(new String(runBazelBytes(args), StandardCharsets.UTF_8));
        System.out.printf("Returning: %s%n", lines);
        return lines;
    }

    private byte[] runBazelBytes(String... args) {
        try {
            Process process = startProcess(args);
            return ByteStreams.toByteArray(process.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Process startProcess(String... args) throws IOException {
        List<String> argv = new ArrayList<>(args.length + 3);
        argv.add(bazel);
        argv.addAll(Arrays.asList(args));
        if (argv.size() > 1) {
            argv.add(2, BES_BACKEND);
            argv.add(3, PUBLISH_ALL_ACTIONS);
        }

        System.out.printf("Running: %s%n", argv);
        return new ProcessBuilder(argv).start();
    }


    private InputStream runBazelStream(String... args) {
        try {
            Process process = startProcess(args);

            return process.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int parseProcess(Process process) throws IOException, InterruptedException {
        Set<String> messageBuilder = new HashSet<>();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null)
            messageBuilder.add(line.trim());

        String message = String.join("\n", messageBuilder);
        int returnCode = process.waitFor();
        if (returnCode != 0)
            logError(message);
        else
            logMessage(message);
        return returnCode;
    }

    protected void logError(String errorMessage) {
        LogMessageParams params = new LogMessageParams(MessageType.ERROR, errorMessage);
        buildClient.onBuildLogMessage(params);
        throw new RuntimeException(errorMessage);
    }

    protected void logMessage(String message) {
        LogMessageParams params = new LogMessageParams(MessageType.LOG, message);
        buildClient.onBuildLogMessage(params);
    }

    @Override
    public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams sourcesParams) {
        return executeCommand(() -> {
            try {
                Build.QueryResult queryResult = Build.QueryResult.parseFrom(runBazelStream("query", "--output=proto",
                        "(" + sourcesParams.getTargets().stream().map(BuildTargetIdentifier::getUri).collect(Collectors.joining("+")) + ")"));

                List<SourcesItem> sources = queryResult.getTargetList().stream()
                        .map(Build.Target::getRule)
                        .map(rule -> {
                            BuildTargetIdentifier label = new BuildTargetIdentifier(rule.getName());
                            List<SourceItem> items = this.getSourceItems(rule, label);
                            List<String> roots = Lists.newArrayList(Uri.fromAbsolutePath(getSourcesRoot(rule.getName())).toString());
                            SourcesItem item = new SourcesItem(label, items);
                            item.setRoots(roots);
                            return item;
                        })
                        .collect(Collectors.toList());
                return Either.forRight(new SourcesResult(sources));
            } catch (IOException e) {
                return Either.forLeft(new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
            }
        });
    }

    private String getSourcesRoot(String uri) {
        Path path = Paths.get(convertOutputToPath(uri, getWorkspaceRoot()));
        String sourcesRoot = null;
        while (sourcesRoot == null) {
            if (sourceRootPattern.matches(path))
                sourcesRoot = path.toString();
            else if (defaultTestRootPattern.matches(path))
                sourcesRoot = path.toString();
            else {
                path = path.getParent();
                if (path == null)
                    sourcesRoot = getWorkspaceRoot();
            }
        }
        List<String> root = KNOWN_SOURCE_ROOTS.stream().filter(uri::contains).collect(Collectors.toList());
        System.out.println("Roots found for uri " + uri + " :" + Arrays.toString(root.toArray()));
        return getWorkspaceRoot() + (root.size() == 0 ? "" : uri.substring(1, uri.indexOf(root.get(0)) + root.get(0).length()));
    }

    public synchronized String getWorkspaceRoot() {
        if (workspaceRoot == null) {
            workspaceRoot = Iterables.getOnlyElement(runBazelLines("info", "workspace"));
        }
        return workspaceRoot;
    }

    public synchronized String getBinRoot() {
        if (binRoot == null) {
            binRoot = Iterables.getOnlyElement(runBazelLines("info", "bazel-bin"));
        }
        return binRoot;
    }

    public synchronized String getExecRoot() {
        if (execRoot == null) {
            execRoot = Iterables.getOnlyElement(runBazelLines("info", "execution_root"));
        }
        return execRoot;
    }

    @Override
    public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
            InverseSourcesParams inverseSourcesParams) {
        return executeCommand(() -> {
            String fileUri = inverseSourcesParams.getTextDocument().getUri();
            String workspaceRoot = getWorkspaceRoot();
            String prefix = Uri.fromWorkspacePath("", workspaceRoot).toString();
            if (!inverseSourcesParams.getTextDocument().getUri().startsWith(prefix)) {
                throw new RuntimeException(
                        "Could not resolve " + fileUri + " within workspace " + prefix);
            }
            try {
                Build.QueryResult result = Build.QueryResult.parseFrom(
                        runBazelStream("query", "--output=proto", "kind(rule, rdeps(//..., " + fileUri.substring(prefix.length()) + ", 1))"));
                List<BuildTargetIdentifier> targets = result.getTargetList().stream()
                        .map(Build.Target::getRule)
                        .map(Build.Rule::getName)
                        .map(BuildTargetIdentifier::new)
                        .collect(Collectors.toList());

                return Either.forRight(new InverseSourcesResult(targets));
            } catch (IOException e) {
                return Either.forLeft(new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
            }
        });
    }

    @Override
    public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
            DependencySourcesParams dependencySourcesParams) {
        return executeCommand(() -> {
            List<String> targets =
                    dependencySourcesParams.getTargets().stream()
                            .map(BuildTargetIdentifier::getUri)
                            .collect(Collectors.toList());

            DependencySourcesResult result = new DependencySourcesResult(
                    targets.stream().sorted()
                            .map(
                                    target -> {
                                        List<String> files =
                                                lookupTransitiveSourceJars(target).stream()
                                                        .map(execPath -> Uri.fromExecPath(execPath, getExecRoot()).toString())
                                                        .collect(Collectors.toList());
                                        return new DependencySourcesItem(new BuildTargetIdentifier(target), files);
                                    })
                            .collect(Collectors.toList()));
            return Either.forRight(result);
        });
    }

    private List<String> runBazelStderr(String... args) {
        try {
            Process process = startProcess(args);
            List<String> output = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line.trim());
            }
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private List<String> lookupTransitiveSourceJars(String target) {
        // TODO(illicitonion): Use an aspect output group, rather than parsing stderr logging
        List<String> lines =
                runBazelStderr("build", "--aspects", "@//.bazelbsp:aspects.bzl%print_aspect", target);
        return lines.stream()
                .map(line -> Splitter.on(" ").splitToList(line))
                .filter(
                        parts ->
                                parts.size() == 3
                                        && parts.get(0).equals("DEBUG:")
                                        && parts.get(1).contains(".bazelbsp/aspects.bzl")
                                        && parts.get(2).endsWith(".jar"))
                .map(parts -> "exec-root://" + parts.get(2))
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams resourcesParams) {
        return executeCommand(() -> {
            try {
                Build.QueryResult query = Build.QueryResult.parseFrom(
                        runBazelStream("query", "--output=proto", "//..."));
                System.out.println("Resources query result " + query);
                ResourcesResult resourcesResult =
                        new ResourcesResult(
                                query.getTargetList()
                                        .stream().map(Build.Target::getRule)
                                        .filter(rule -> resourcesParams.getTargets().stream().anyMatch(target -> target.getUri().equals(rule.getName())))
                                        .filter(rule -> rule.getAttributeList().stream()
                                                .anyMatch(attribute ->
                                                        attribute.getName().equals("resources") && attribute.hasExplicitlySpecified() && attribute.getExplicitlySpecified())
                                        )
                                        .map(rule -> new ResourcesItem(
                                                new BuildTargetIdentifier(rule.getName()),
                                                getResources(rule, query)))
                                        .collect(Collectors.toList()));
                return Either.forRight(resourcesResult);
            } catch (IOException e) {
                return Either.forLeft(
                        new ResponseError(
                                ResponseErrorCode.InternalError,
                                e.getMessage(),
                                null
                        )
                );
            }
        });
    }

    private List<String> getResources(Build.Rule rule, Build.QueryResult queryResult) {
        return rule.getAttributeList().stream()
                .filter(attribute -> attribute.getName().equals("resources") && attribute.hasExplicitlySpecified() && attribute.getExplicitlySpecified())
                .flatMap(
                        attribute -> {
                            List<Build.Target> targetsRule = attribute.getStringListValueList().stream()
                                    .map(label -> isPackage(queryResult, label))
                                    .filter(targets -> !targets.isEmpty())
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());
                            List<String> targetsResources = getResourcesOutOfRule(targetsRule);

                            List<String> resources = attribute.getStringListValueList().stream()
                                    .filter(label -> isPackage(queryResult, label).isEmpty())
                                    .map(label -> Uri.fromFileLabel(label, getWorkspaceRoot()).toString())
                                    .collect(Collectors.toList());

                            return Stream.concat(targetsResources.stream(), resources.stream());
                        }
                )
                .collect(Collectors.toList());
    }

    private List<? extends Build.Target> isPackage(Build.QueryResult queryResult, String label) {
        return queryResult.getTargetList().stream()
                .filter(target -> target.hasRule() && target.getRule().getName().equals(label))
                .collect(Collectors.toList());
    }

    private List<String> getResourcesOutOfRule(List<Build.Target> rules) {
        return rules.stream()
                .flatMap(resourceRule -> resourceRule.getRule().getAttributeList().stream())
                .filter((srcAttribute) -> srcAttribute.getName().equals("srcs"))
                .flatMap(resourceAttribute -> resourceAttribute.getStringListValueList().stream())
                .map(src -> Uri.fromFileLabel(src, getWorkspaceRoot()).toString())
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<CompileResult> buildTargetCompile(CompileParams compileParams) {
        return executeCommand(() -> buildTargetsWithBep(compileParams.getTargets(), new ArrayList<>()));
    }

    private Either<ResponseError, CompileResult> buildTargetsWithBep(List<BuildTargetIdentifier> targets, List<String> extraFlags) {
        List<String> args = Lists.newArrayList(
                bazel,
                "build",
                BES_BACKEND,
                PUBLISH_ALL_ACTIONS
        );
        args.addAll(
                targets.stream()
                        .map(BuildTargetIdentifier::getUri)
                        .collect(Collectors.toList()));
        args.addAll(extraFlags);
        int exitCode = -1;

        final Map<String, String> diagnosticsProtosLocations = bepServer.getDiagnosticsProtosLocations();
        try {
            Build.QueryResult queryResult = Build.QueryResult.parseFrom(
                    runBazelStream("query", "--output=proto",
                            "(" + targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.joining("+")) + ")")
            );


            for (Build.Target target : queryResult.getTargetList()) {
                target.getRule().getRuleOutputList()
                        .stream()
                        .filter(output -> output.contains("diagnostics"))
                        .forEach(output -> diagnosticsProtosLocations.put(target.getRule().getName(), convertOutputToPath(output, getBinRoot())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            getExecRoot();
            getWorkspaceRoot();
            getBinRoot();
            Process process = new ProcessBuilder(args).start();
            exitCode = parseProcess(process);
        } catch (InterruptedException | IOException e) {
            System.out.println("Failed to run bazel: " + e);
        }

        for (Map.Entry<String, String> diagnostics : diagnosticsProtosLocations.entrySet()) {
            String target = diagnostics.getKey();
            String diagnosticsPath = diagnostics.getValue();
            Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics = new HashMap<>();
            try {
                BuildTargetIdentifier targetIdentifier = new BuildTargetIdentifier(target);
                bepServer.getDiagnostics(filesToDiagnostics, targetIdentifier, diagnosticsPath);
                bepServer.emitDiagnostics(filesToDiagnostics, targetIdentifier);
            } catch (IOException e) {
                System.err.println("Failed to get diagnostics for " + target);
            }
        }

        return Either.forRight(new CompileResult(BepServer.convertExitCode(exitCode)));
    }

    private String convertOutputToPath(String output, String prefix) {
        String pathToFile = output.replaceAll("(//|:)", "/");
        return prefix + pathToFile;
    }

    @Override
    public CompletableFuture<TestResult> buildTargetTest(TestParams testParams) {
        return executeCommand(() -> {
            Either<ResponseError, CompileResult> build = buildTargetsWithBep(Lists.newArrayList(testParams.getTargets()), new ArrayList<>());
            if (build.isLeft())
                return Either.forLeft(build.getLeft());

            CompileResult result = build.getRight();
            if (result.getStatusCode() != StatusCode.OK)
                return Either.forRight(new TestResult(result.getStatusCode()));

            try {
                Process process = startProcess(
                        Lists.asList(
                                "test",
                                "(" +
                                        Joiner.on("+").join(testParams.getTargets().stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList()))
                                        + ")",
                                testParams.getArguments().toArray(new String[0])
                        ).toArray(new String[0])
                );

                int returnCode = parseProcess(process);
                return Either.forRight(new TestResult(BepServer.convertExitCode(returnCode)));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<RunResult> buildTargetRun(RunParams runParams) {
        return executeCommand(() -> {
            Either<ResponseError, CompileResult> build = buildTargetsWithBep(Lists.newArrayList(runParams.getTarget()), new ArrayList<>());
            if (build.isLeft())
                return Either.forLeft(build.getLeft());

            CompileResult result = build.getRight();
            if (result.getStatusCode() != StatusCode.OK)
                return Either.forRight(new RunResult(result.getStatusCode()));

            try {
                Process process = startProcess(
                        Lists.asList(
                                "run",
                                runParams.getTarget().getUri(),
                                runParams.getArguments().toArray(new String[0])
                        ).toArray(new String[0])
                );

                int returnCode = parseProcess(process);
                return Either.forRight(new RunResult(BepServer.convertExitCode(returnCode)));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<CleanCacheResult> buildTargetCleanCache(
            CleanCacheParams cleanCacheParams) {
        return executeCommand(() -> {
            CleanCacheResult result;
            try {
                result = new CleanCacheResult(String.join("\n", runBazelLines("clean")), true);
            } catch (RuntimeException e) {
                result = new CleanCacheResult(e.getMessage(), false);
            }
            return Either.forRight(result);
        });
    }

    @Override
    public CompletableFuture<ScalacOptionsResult> buildTargetScalacOptions(
            ScalacOptionsParams scalacOptionsParams) {
        return executeCommand(() -> {
            List<String> targets =
                    scalacOptionsParams.getTargets().stream()
                            .map(BuildTargetIdentifier::getUri)
                            .collect(Collectors.toList());
            String targetsUnion = Joiner.on(" + ").join(targets);
            Map<String, List<String>> targetsOptions = getTargetsOptions(targetsUnion, "scalacopts");
            Either<ResponseError, ActionGraphParser> either = parseActionGraph(getMnemonics(targetsUnion, Lists.newArrayList(SCALAC, JAVAC)));
            if (either.isLeft())
                return Either.forLeft(either.getLeft());

            ScalacOptionsResult result = new ScalacOptionsResult(
                    targets.stream()
                            .flatMap(target ->
                                    collectScalacOptionsResult(
                                            either.getRight(),
                                            targetsOptions.getOrDefault(target, new ArrayList<>()),
                                            either.getRight().getInputsAsUri(target, getExecRoot()),
                                            target))
                            .collect(Collectors.toList()));
            return Either.forRight(result);
        });
    }


    @Override
    public CompletableFuture<JavacOptionsResult> buildTargetJavacOptions(JavacOptionsParams javacOptionsParams) {
        return executeCommand(() -> {
            List<String> targets =
                    javacOptionsParams.getTargets().stream()
                            .map(BuildTargetIdentifier::getUri)
                            .collect(Collectors.toList());

            String targetsUnion = Joiner.on(" + ").join(targets);
            Map<String, List<String>> targetsOptions = getTargetsOptions(targetsUnion, "javacopts");
            // TODO(andrefmrocha): Remove this when kotlin is natively supported
            Either<ResponseError, ActionGraphParser> either =
                    parseActionGraph(getMnemonics(targetsUnion, Lists.newArrayList(JAVAC, KOTLINC)));
            if (either.isLeft())
                return Either.forLeft(either.getLeft());

            JavacOptionsResult result = new JavacOptionsResult(
                    targets.stream()
                            .flatMap(target ->
                                    collectJavacOptionsResult(
                                            either.getRight(),
                                            targetsOptions.getOrDefault(target, new ArrayList<>()),
                                            either.getRight().getInputsAsUri(target, getExecRoot()),
                                            target))
                            .collect(Collectors.toList()));
            return Either.forRight(result);
        });
    }

    private Map<String, List<String>> getTargetsOptions(String targetsUnion, String compilerOptionsName) {
        try {
            Build.QueryResult query = Build.QueryResult.parseFrom(
                    runBazelStream("query", "--output=proto", "(" + targetsUnion + ")")
            );

            return query
                    .getTargetList().stream()
                    .map(Build.Target::getRule)
                    .collect(
                            Collectors.toMap(
                                    Build.Rule::getName,
                                    (rule) -> rule.getAttributeList().stream()
                                            .filter(attr -> attr.getName().equals(compilerOptionsName))
                                            .flatMap(attr -> attr.getStringListValueList().stream())
                                            .collect(Collectors.toList())
                            )
                    );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<JavacOptionsItem> collectJavacOptionsResult(
            ActionGraphParser actionGraphParser,
            List<String> options,
            List<String> inputs,
            String target) {
        return actionGraphParser
                .getOutputs(target, Lists.newArrayList(".jar", ".js"))
                .stream().map(output ->
                        new JavacOptionsItem(
                                new BuildTargetIdentifier(target),
                                options,
                                inputs,
                                Uri.fromExecPath("exec-root://" + output, execRoot).toString())
                );
    }

    private Either<ResponseError, ActionGraphParser> parseActionGraph(String query) {
        try {
            AnalysisProtos.ActionGraphContainer actionGraph = AnalysisProtos.ActionGraphContainer.parseFrom(
                    runBazelBytes(
                            "aquery",
                            "--output=proto",
                            query));
            return Either.forRight(new ActionGraphParser(actionGraph));
        } catch (IOException e) {
            return Either.forLeft(new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
        }
    }

    private String getMnemonics(String targetsUnion, List<String> languageIds) {
        return languageIds.stream()
                .filter(Objects::nonNull)
                .map(mnemonic -> "mnemonic(" + mnemonic + ", " + targetsUnion + ")")
                .collect(Collectors.joining(" union "));
    }

    private Stream<ScalacOptionsItem> collectScalacOptionsResult(
            ActionGraphParser actionGraphParser,
            List<String> options,
            List<String> inputs,
            String target) {
        List<String> suffixes = Lists.newArrayList(".jar", ".js");
        return actionGraphParser.getOutputs(target, suffixes)
                .stream().map(output ->
                        new ScalacOptionsItem(
                                new BuildTargetIdentifier(target),
                                options,
                                inputs,
                                Uri.fromExecPath("exec-root://" + output, execRoot).toString())
                );
    }

    @Override
    public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
            ScalaTestClassesParams scalaTestClassesParams) {
        System.out.printf("DWH: Got buildTargetScalaTestClasses: %s%n", scalaTestClassesParams);
        // TODO(illicitonion): Populate
        return CompletableFuture.completedFuture(new ScalaTestClassesResult(new ArrayList<>()));
    }

    @Override
    public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
            ScalaMainClassesParams scalaMainClassesParams) {
        System.out.printf("DWH: Got buildTargetScalaMainClasses: %s%n", scalaMainClassesParams);
        // TODO(illicitonion): Populate
        return CompletableFuture.completedFuture(new ScalaMainClassesResult(new ArrayList<>()));
    }

    public Iterable<SourceItem> getCachedBuildTargetSources(BuildTargetIdentifier target) {
        if (targetsToSources.containsKey(target))
            return targetsToSources.get(target);

        try {
            Build.QueryResult queryResult = Build.QueryResult.parseFrom(
                    runBazelStream("query", "--output=proto", target.getUri()));

            return queryResult.getTargetList().stream()
                    .map(Build.Target::getRule)
                    .flatMap(rule -> this.getSourceItems(rule, target).stream())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Failed to query for sources of target " + target);
            return new ArrayList<>();
        }
    }

    public void setBuildClient(BuildClient buildClient) {
        this.buildClient = buildClient;
    }
}
