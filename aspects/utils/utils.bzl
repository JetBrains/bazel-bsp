def abs(num):
    if num < 0:
        return -num
    else:
        return num

def map(f, xs):
    return [f(x) for x in xs]

def filter(f, xs):
    return [x for x in xs if f(x)]

def flatten(xss):
    return [x for xs in xss for x in xs]

def flatmap(f, xs):
    return flatten(map(f, xs))

def file_location(file):
    if file == None:
        return None

    return to_file_location(
        file.path,
        file.root.path if not file.is_source else "",
        file.is_source,
        file.owner.workspace_root.startswith("..") or file.owner.workspace_root.startswith("external"),
    )

def _strip_root_exec_path_fragment(path, root_fragment):
    if root_fragment and path.startswith(root_fragment + "/"):
        return path[len(root_fragment + "/"):]
    return path

def _strip_external_workspace_prefix(path):
    if path.startswith("../") or path.startswith("external/"):
        return "/".join(path.split("/")[2:])
    return path

def to_file_location(exec_path, root_exec_path_fragment, is_source, is_external):
    # directory structure:
    # exec_path = (../repo_name)? + (root_fragment)? + relative_path
    relative_path = _strip_external_workspace_prefix(exec_path)
    relative_path = _strip_root_exec_path_fragment(relative_path, root_exec_path_fragment)

    root_exec_path_fragment = exec_path[:-(len("/" + relative_path))] if relative_path != "" else exec_path

    return struct(
        relative_path = relative_path,
        is_source = is_source,
        is_external = is_external,
        root_execution_path_fragment = root_exec_path_fragment,
    )

def create_struct(**kwargs):
    d = {name: kwargs[name] for name in kwargs if kwargs[name] != None}
    return struct(**d)

def update_sync_output_groups(groups_dict, key, new_set):
    update_set_in_dict(groups_dict, key + "-transitive-deps", new_set)
    update_set_in_dict(groups_dict, key + "-outputs", new_set)
    update_set_in_dict(groups_dict, key + "-direct-deps", new_set)

def update_set_in_dict(input_dict, key, other_set):
    input_dict[key] = depset(transitive = [input_dict.get(key, depset()), other_set])

def get_aspect_ids(ctx, target):
    """Returns the all aspect ids, filtering out self."""
    aspect_ids = None
    if hasattr(ctx, "aspect_ids"):
        aspect_ids = ctx.aspect_ids
    elif hasattr(target, "aspect_ids"):
        aspect_ids = target.aspect_ids
    else:
        return None
    return [aspect_id for aspect_id in aspect_ids if "bsp_target_info_aspect" not in aspect_id]

def create_proto(target, ctx, data, name):
    if data == None:
        return None

    aspect_ids = get_aspect_ids(ctx, target)
    file_name = target.label.name
    file_name = file_name + "-" + str(abs(hash(file_name)))
    if aspect_ids:
        file_name = file_name + "-" + str(abs(hash(".".join(aspect_ids))))
    file_name = "%s.%s" % (file_name, name)
    file_name = "%s.bsp-info.textproto" % file_name
    info_file = ctx.actions.declare_file(file_name)
    ctx.actions.write(info_file, data.to_proto())
    return info_file

def is_external(target):
    return not str(target.label).startswith("@@//") and not str(target.label).startswith("@//") and not str(target.label).startswith("//")

def convert_struct_to_dict(s):
    attrs = dir(s)

    # two deprecated methods of struct
    if "to_json" in attrs:
        attrs.remove("to_json")
    if "to_proto" in attrs:
        attrs.remove("to_proto")

    return {key: getattr(s, key) for key in attrs}
