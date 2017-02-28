from setuptools import setup
from os.path import dirname, join

# Read version and requires from generated file.
info = dict()
with open(join(dirname(__file__), 'version.py')) as stream:
    exec(stream.read(), info)

setup(
    name='simple',
    version=info['__version__'],
    packages=['simple'],
    install_requires= info['__requires__']
)
