import os
import parser
import sys
from random import choice
import random


def randname():
    return ''.join(
        (choice(CHARSET) for _ in range(NAMELEN))
    )


def create_java_package(workspace_dir, packagename, deps, number_of_sources):
    packagedir = f'{workspace_dir}/{packagename}'
    os.mkdir(packagedir)
    with open(f'{packagedir}/BUILD', 'w') as f:
        f.write(f"""java_library(
          name = "{packagename}",
          visibility = ["//visibility:public"],
          srcs = glob(["*.java"]),
          deps = {deps}
        )\n""")
    for i in range(number_of_sources):
        classname = f"Lib_{packagename}_{i}"
        with open(f'{packagedir}/{classname}.java', 'w') as f:
            f.write(f"""public class {classname} {"{}"}\n""")
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

args = parser.parser().parse_args()

project_dir = args.projectdir
number_of_targets = args.targets
targets = [randname() for x in range(number_of_targets)]

if not os.path.exists(f'./{project_dir}'):
    os.mkdir(project_dir)
else:
    print('This directory already exists.')

dependency_graph = build_dependency_graph(targets)

for target, deps in dependency_graph.items():
    create_java_package(f'{project_dir}/', target, [f'//{d}:{d}' for d in deps], args.targetssize)

with open(f'{project_dir}/WORKSPACE', 'w') as fp:
    pass

print(f'Generated {number_of_targets} targets')
