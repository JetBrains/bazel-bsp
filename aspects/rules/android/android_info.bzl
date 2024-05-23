load("//aspects:utils/utils.bzl", "create_file_location", "create_struct", "file_location")

ANDROID_SDK_TOOLCHAIN_TYPE = "@bazel_tools//tools/android:sdk_toolchain_type"

def extract_android_info(target, ctx, dep_targets, **kwargs):
    if ANDROID_SDK_TOOLCHAIN_TYPE not in ctx.toolchains:
        return None, None
    android_sdk_toolchain = ctx.toolchains[ANDROID_SDK_TOOLCHAIN_TYPE]

    if android_sdk_toolchain == None:
        return None, None
    android_sdk_info = android_sdk_toolchain.android_sdk_info
    android_jar = file_location(android_sdk_info.android_jar)
    if android_jar == None:
        return None, None

    manifest = None
    if hasattr(ctx.rule.attr, "manifest") and ctx.rule.attr.manifest:
        manifest_files = ctx.rule.attr.manifest.files.to_list()
        if manifest_files:
            manifest = file_location(manifest_files[0])

    resource_directories_set = {}
    if hasattr(ctx.rule.attr, "resource_files"):
        for resource in ctx.rule.attr.resource_files:
            for resource_file in resource.files.to_list():
                resource_file_location = file_location(resource_file)
                resource_source_dir_relative_path = android_common.resource_source_directory(resource_file)
                if resource_source_dir_relative_path == None:
                    continue
                resource_source_dir_location = \
                    set_relative_path(resource_file_location, resource_source_dir_relative_path)

                # Add to set
                resource_directories_set[resource_source_dir_location] = None

    resource_java_package = None
    if hasattr(ctx.rule.attr, "custom_package"):
        resource_java_package = ctx.rule.attr.custom_package

    assets_directories = []
    if hasattr(ctx.rule.attr, "assets") and ctx.rule.attr.assets and \
       hasattr(ctx.rule.attr, "assets_dir") and ctx.rule.attr.assets_dir:
        first_asset_files = ctx.rule.attr.assets[0].files.to_list()
        if first_asset_files:
            first_asset = first_asset_files[0]
            first_asset_location = file_location(first_asset)
            asset_folder_relative_path = ctx.label.package + "/" + ctx.rule.attr.assets_dir
            asset_folder_location = set_relative_path(first_asset_location, asset_folder_relative_path)
            assets_directories.append(asset_folder_location)

    aidl_binary_jar = None
    aidl_source_jar = None
    if AndroidIdeInfo in target:
        android_ide_info = target[AndroidIdeInfo]
        if android_ide_info.idl_class_jar:
            aidl_binary_jar = file_location(android_ide_info.idl_class_jar)
        if android_ide_info.idl_source_jar:
            aidl_source_jar = file_location(android_ide_info.idl_source_jar)

    android_target_info = create_struct(
        android_jar = android_jar,
        manifest = manifest,
        resource_directories = resource_directories_set.keys(),
        resource_java_package = resource_java_package,
        assets_directories = assets_directories,
        aidl_binary_jar = aidl_binary_jar,
        aidl_source_jar = aidl_source_jar,
    )

    return dict(android_target_info = android_target_info), None

def set_relative_path(location, new_relative_path):
    return create_file_location(
        relative_path = new_relative_path,
        is_source = location.is_source,
        is_external = location.is_external,
        root_execution_path_fragment = location.root_execution_path_fragment,
    )

def extract_android_aar_import_info(target, ctx, dep_targets, **kwargs):
    if ctx.rule.kind != "aar_import":
        return None, None

    if AndroidManifestInfo not in target:
        return None, None
    manifest = file_location(target[AndroidManifestInfo].manifest)

    resource_folder = None
    r_txt = None
    if AndroidResourcesInfo in target:
        direct_android_resources = target[AndroidResourcesInfo].direct_android_resources.to_list()
        if direct_android_resources:
            direct_android_resource = direct_android_resources[0]
            resource_files = direct_android_resource.resources
            if resource_files:
                resource_folder = file_location(resource_files[0])
            r_txt = file_location(direct_android_resource.r_txt)

    android_aar_import_info = create_struct(
        manifest = manifest,
        resource_folder = resource_folder,
        r_txt = r_txt,
    )
    return dict(android_aar_import_info = android_aar_import_info), None
