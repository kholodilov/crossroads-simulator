export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/runtime/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/bin/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/os/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/java/jre/glnxa64/jre/lib/amd64/native_threads:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/java/jre/glnxa64/jre/lib/amd64/server:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/java/jre/glnxa64/jre/lib/amd64

export XAPPLRESDIR=/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/X11/app-defaults

java -cp /usr/share/java/clojure-1.4.jar:/usr/local/MATLAB/MATLAB_Production_Server/R2013a/toolbox/javabuilder/jar/glnxa64/javabuilder.jar:SwitchLights/distrib/SwitchLights.jar clojure.main SwitchLights.clj
