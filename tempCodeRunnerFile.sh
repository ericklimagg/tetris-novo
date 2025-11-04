
# 1. Limpa builds antigos
echo "Limpando builds antigos (pasta bin/ e arquivos .class)..."
rm -rf bin
find src -name "*.class" -delete

# 2. Cria a pasta de 'build' (saída)
echo "Criando pasta bin..."
mkdir bin

# 3. Compila TODOS os arquivos .java da 'src' para dentro da 'bin'
echo "Compilando código-fonte (.java)..."
javac -d bin -cp src $(find src -name "*.java")

# 4. Verifica se a compilação falhou
if [ $? -ne 0 ]; then
    echo "----------------------------------------"
    echo "ERRO: Falha na compilação."
    echo "Corrija os erros de código e tente novamente."
    echo "----------------------------------------"
    exit 1
fi

# 5. Copia TODOS os recursos (áudio, txt, etc.) da 'src' para a 'bin'
echo "Copiando todos os recursos (áudio, txt, etc.) para a pasta 'bin'..."
rsync -av --prune-empty-dirs --exclude="*.java" src/ bin/
# Copia também o highscore.txt da raiz
cp highscore.txt bin/

# 6. Executa o programa!
echo "----------------------------------------"
echo "Build concluído. Iniciando o Jogo..."
echo "----------------------------------------"
java -cp bin com.tetris.Main
