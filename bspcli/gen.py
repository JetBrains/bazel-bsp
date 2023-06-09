import os
import sys
from random import choice
import random


def randname():
    return ''.join(
        (choice(CHARSET) for _ in range(NAMELEN))
    )


def create_package(workspace_dir, packagename, deps):
    packagedir = f'{workspace_dir}/{packagename}'
    os.mkdir(packagedir)
    classname = "Lib_" + packagename
    with open(f'{packagedir}/BUILD', 'w') as f:
        f.write(f"""java_library(
          name = "{packagename}",
          visibility = ["//visibility:public"],
          srcs = ["{classname}.java"],
          deps = {deps}
        )\n""")
    with open(f'{packagedir}/{classname}.java', 'w') as f:
        f.write(f"""class {classname} {"{}"}\n""")
        pass


def build_dependency_graph(targets):
    dependency_graph = {}
    while len(targets) > 0:
        number_of_children = random.randrange(0, 5)
        parent = targets[0]
        targets = targets[1:]
        children = []
        for i in range(number_of_children):
            children = children + ([choice(targets)] if targets else [])
        dependency_graph[parent] = set(children)
    return dependency_graph


CHARSET = 'abcdefghijklmnopqrstuvwxyz'
NAMELEN: int = 8
random.seed(0)
project_dir = sys.argv[1]
number_of_targets = int(sys.argv[2])
targets = [randname() for x in range(number_of_targets)]

if not os.path.exists(f'./{project_dir}'):
    os.mkdir(project_dir)
else:
    print('This directory already exists.')

dependency_graph = build_dependency_graph(targets)

for target, deps in dependency_graph.items():
    create_package(f'{project_dir}/', target, [f'//{d}:{d}' for d in deps])

with open(f'{project_dir}/WORKSPACE', 'w') as fp:
    pass

print(f'Generated {number_of_targets} targets')
