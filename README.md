# Tetris (Java Swing)

Este é um clone do clássico jogo Tetris feito em Java (Swing), com foco em um sistema robusto de perfis de jogador e ranking. O projeto salva todas as pontuações e estatísticas de jogador em um banco de dados SQL Server.

### Funcionalidades
* Modo de 1 Jogador (1P) e 2 Jogadores (2P)
* Sistema de perfis de usuário (Criar e Selecionar)
* Ranking 1P (baseado na maior pontuação por usuário)
* Ranking 2P (baseado no total de vitórias)
* Temas visuais customizáveis
* Peça "Fantasma" (Ghost Piece)
* Música de fundo

---

## 🛠️ Como Configurar e Rodar o Projeto

Este projeto requer um JDK, o driver JDBC do SQL Server, e um servidor SQL Server rodando.

### 1. Pré-requisitos

1.  **Java JDK 11 (ou superior):** Você precisa do JDK (Java Development Kit) para compilar o código.
2.  **Microsoft SQL Server:** O jogo precisa de uma instância do SQL Server rodando. A forma mais fácil é usar Docker:
    ```bash
    docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=seuPasswordForte" -p 1433:1433 -d [mcr.microsoft.com/mssql/server](https://mcr.microsoft.com/mssql/server)
    ```
    *(Substitua `seuPasswordForte` por uma senha de sua escolha)*.
3.  **Git:** Para clonar o repositório.

### 2. Configuração do Projeto

1.  **Clone o Repositório:**
    ```bash
    git clone [https://github.com/seu-usuario/tetris-novo.git](https://github.com/seu-usuario/tetris-novo.git)
    cd tetris-novo
    ```
    *(Lembre-se de trocar `seu-usuario/tetris-novo` pelo URL real do seu repositório)*.

2.  **Baixe o Driver JDBC:**
    O script de execução espera o driver do SQL Server. Como ele não está no repositório (ignorado pelo `.gitignore`), você deve baixá-lo manually.
    * Crie a pasta `lib/` na raiz do projeto.
    * Baixe o **Microsoft JDBC Driver for SQL Server** (arquivo `.jar`).
    * Coloque o arquivo `mssql-jdbc-13.2.1.jre11.jar` dentro da pasta `lib/`.

3.  **Crie o Arquivo de Configuração:**
    Este projeto usa um arquivo `config.properties` (ignorado pelo Git) para armazenar suas senhas.
    * Crie o arquivo `config.properties` na raiz do projeto.
    * Copie e cole o conteúdo abaixo, **alterando o usuário e a senha** para corresponder à configuração do seu servidor SQL:

    ```properties
    # Configurações do Banco de Dados
    db.host=localhost
    db.port=1433
    db.name=TetrisDB
    db.user=sa
    db.password=seuPasswordForte
    ```

4.  **Configure o Banco de Dados:**
    * Usando sua ferramenta de banco de dados (SSMS, Azure Data Studio, etc.), conecte-se à sua instância do SQL Server.
    * Crie um novo banco de dados. O nome padrão no `config.properties` é **`TetrisDB`**.
        ```sql
        CREATE DATABASE TetrisDB;
        ```
    * Execute o script `schema.sql` (incluído neste repositório) dentro do seu banco `TetrisDB` para criar todas as tabelas (`PlayerProfiles`, `SoloScores`, etc.).

---

### 3. Como Rodar o Jogo

#### 🐧 Em Linux / macOS

O script `run.sh` automatiza tudo para você.

```bash
# 1. Dê permissão de execução ao script (apenas na primeira vez)
chmod +x run.sh

# 2. Execute o script
./run.sh para linux
ou
./run.bat para windows
