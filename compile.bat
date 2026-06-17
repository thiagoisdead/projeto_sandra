@echo off
if not exist "bin" mkdir "bin"
javac -cp "lib/*" src/portaria/model/*.java src/portaria/database/*.java src/portaria/exception/*.java src/portaria/dao/*.java src/portaria/view/*.java src/portaria/util/*.java src/portaria/Main.java -d bin
echo Compilacao concluida.

