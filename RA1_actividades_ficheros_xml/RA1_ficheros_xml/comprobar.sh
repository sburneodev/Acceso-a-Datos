#!/bin/sh
# Equivalente de comprobar.bat para Linux y macOS.
cd "$(dirname "$0")" || exit 1
java Comprobador.java
