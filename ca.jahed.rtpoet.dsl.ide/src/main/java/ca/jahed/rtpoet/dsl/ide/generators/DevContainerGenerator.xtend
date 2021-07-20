package ca.jahed.rtpoet.dsl.ide.generator

class DevContainerGenerator {

	def static generate() {'''
     {
        "name": "UML-RT Development",
        "image": "kjahed/vscode-rtpoetdev:latest",
        "remoteUser": "vscode",
        "workspaceMount": "source=${localWorkspaceFolder},target=/workspace,type=bind,consistency=delegated",
        "workspaceFolder": "/workspace",

        "runArgs": [
            "--cap-add=SYS_PTRACE",
            "--security-opt",
            "seccomp=unconfined"
        ],

        "settings": {
            "terminal.integrated.shell.linux": "/bin/zsh",

            "C_Cpp.default.includePath": [
                "/workspace/**",
                "/umlrts/include/**"
            ],

            "C_Cpp.default.compilerPath": "/usr/bin/g++",
            "C_Cpp.default.cStandard": "gnu11",
            "C_Cpp.default.cppStandard": "gnu++11",
            "C_Cpp.default.intelliSenseMode": "linux-gcc-x64"
        },

        "extensions": [
            "ms-vscode.cpptools",
            "kjahed.rtpoet-vscode-extension"
        ]

        // Use 'forwardPorts' to make a list of ports inside the container available locally.
        // "forwardPorts": [],

        // Use 'postCreateCommand' to run commands after the container is created.
        // "postCreateCommand": "gcc -v",
     }
	'''}
}
