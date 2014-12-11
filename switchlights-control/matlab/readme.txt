export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/runtime/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/bin/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/os/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/java/jre/glnxa64/jre/lib/amd64/native_threads:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/java/jre/glnxa64/jre/lib/amd64/server:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/sys/java/jre/glnxa64/jre/lib/amd64

export XAPPLRESDIR=/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81/X11/app-defaults

java -cp /usr/share/java/clojure-1.4.jar:/usr/local/MATLAB/MATLAB_Production_Server/R2013a/toolbox/javabuilder/jar/glnxa64/javabuilder.jar:SwitchLights/distrib/SwitchLights.jar clojure.main SwitchLights.clj

----

export XAPPLRESDIR=/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/X11/app-defaults

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/runtime/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/bin/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/sys/os/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/sys/java/jre/glnxa64/jre/lib/amd64/native_threads:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/sys/java/jre/glnxa64/jre/lib/amd64/server:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/sys/java/jre/glnxa64/jre/lib/amd64

java -cp /usr/share/java/clojure-1.4.jar:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v83/toolbox/javabuilder/jar/glnxa64/javabuilder.jar:/home/dmitry/Downloads/CrossroadControl/for_testing/CrossroadControl.jar clojure.main CrossroadControl.clj

----

export DYLD_LIBRARY_PATH=/Applications/MATLAB/MATLAB_Compiler_Runtime/v83/runtime/maci64:/Applications/MATLAB/MATLAB_Compiler_Runtime/v83/sys/os/maci64:/Applications/MATLAB/MATLAB_Compiler_Runtime/v83/bin/maci64

java -cp /Users/dmitry/.m2/repository/org/clojure/clojure/1.6.0/clojure-1.6.0.jar:/Applications/MATLAB/MATLAB_Compiler_Runtime/v83/toolbox/javabuilder/jar/glnxa64/javabuilder.jar:/Users/dmitry/Downloads/CrossroadControl/for_testing/CrossroadControl.jar clojure.main CrossroadControl.clj
