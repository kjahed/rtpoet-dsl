package ca.jahed.rtpoet.dsl.ide.generator

import ca.jahed.rtpoet.rtmodel.*;

class DevContainerGenerator {

	def static generate(RTModel model) {'''
     {
        "name": "C++",
        "image": "kjahed/vscode-rtpoetdev",
        "remoteUser": "vscode",
        "workspaceMount": "source=${localWorkspaceFolder}/src-gen/cpp/src,target=/workspace,type=bind,consistency=delegated",
        "workspaceFolder": "/workspace",

        "containerEnv": {
            "TOP_NAME": "«model.top.capsule.name»"
        },

        "runArgs": [
            "--cap-add=SYS_PTRACE",
            "--security-opt",
            "seccomp=unconfined"
        ],

        "settings": {
            "terminal.integrated.shell.linux": "/bin/zsh",

            "makefile.configurations": [
                {
                    "name": "«model.top.capsule.name»",
                    "makefilePath": "/workspace/Makefile«model.top.capsule.name».mk"
                }
            ],

            "C_Cpp.default.includePath": [
                "/workspace/**",
                "/umlrts/include/**"
            ],

            "C_Cpp.default.compilerPath": "/usr/bin/g++",
            "C_Cpp.default.cStandard": "gnu11",
            "C_Cpp.default.cppStandard": "gnu++11",
            "C_Cpp.default.intelliSenseMode": "linux-gcc-x64",
            "C_Cpp.default.configurationProvider": "ms-vscode.makefile-tools"
        },

        "extensions": [
            "ms-vscode.cpptools",
            "ms-vscode.makefile-tools"
        ]

        // Use 'forwardPorts' to make a list of ports inside the container available locally.
        // "forwardPorts": [],

        // Use 'postCreateCommand' to run commands after the container is created.
        // "postCreateCommand": "gcc -v",
     }
	'''}
}
