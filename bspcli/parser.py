import argparse
def parser():
    parser = argparse.ArgumentParser(
        prog='Bazel Projects Generator',
        description='Generates large Bazel projects')
    parser.add_argument("projectdir", help="Directory in which the project should be created")
    parser.add_argument("targets", type=int, help="Number of targets in the project")
    parser.add_argument("--targetssize", type=int, help="Number of source files in each target", default=1)
    return parser

