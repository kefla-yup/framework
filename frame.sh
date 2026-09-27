#!/bin/bash
cd "$(dirname "$BASH_SOURCE")" || exit

mkdir -p bin

echo "Compilation des sources..."
javac -cp "lib/*:lib/servlet-api.jar" -d bin $(find src -name "*.java")

if [ $? -ne 0 ]; then
    echo "Erreur de compilation du framework !"
    exit 1
fi

echo "Création du framework.jar..."
jar cvf framework.jar -C bin .

echo "Terminé ! Votre fichier framework.jar est prêt."