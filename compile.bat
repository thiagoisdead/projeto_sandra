@echo off
if not exist "bin" mkdir "bin"
javac -cp "lib/*" src/br/com/reservadaaldeia/portaria/model/*.java src/br/com/reservadaaldeia/portaria/database/*.java src/br/com/reservadaaldeia/portaria/exception/*.java src/br/com/reservadaaldeia/portaria/dao/*.java src/br/com/reservadaaldeia/portaria/view/*.java src/br/com/reservadaaldeia/portaria/util/*.java src/br/com/reservadaaldeia/portaria/Main.java -d bin
echo Compilacao concluida.
