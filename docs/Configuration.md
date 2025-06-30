# Guia de Configuração e Infraestrutura

> Este documento detalha a configuração do build, as dependências do projeto, as variáveis de ambiente e a infraestrutura de containerização do RPG Market.

## 1. `pom.xml` - Dependências e Build do Projeto

O projeto utiliza o **Apache Maven** para gerenciamento de dependências e para o processo de build.

* **Informações do Projeto:**
    * `groupId`: `com.programacao_web`
    * `artifactId`: `rpg_market`
    * `version`: `0.0.1-SNAPSHOT`
    * `java.version`: **17**

* **Principais Dependências de Runtime:**
    * `spring-boot-starter-web`: Fornece o núcleo para aplicações web, incluindo o servidor Tomcat embarcado e o padrão MVC.
    * `spring-boot-starter-security`: Habilita todo o sistema de autenticação e autorização de rotas.
    * `spring-boot-starter-data-mongodb`: Fornece a integração com o banco de dados MongoDB através do Spring Data.
    * `spring-boot-starter-thymeleaf`: Motor de templates para renderizar as páginas HTML no lado do servidor.
    * `thymeleaf-extras-springsecurity6`: Permite o uso de tags de segurança do Spring dentro dos templates Thymeleaf (ex: `sec:authorize`).
    * `spring-boot-starter-actuator`: Expõe endpoints de monitoramento, como `/actuator/health`.
    * `spring-boot-devtools`: Utilitário para desenvolvimento que habilita live-reload e outras funcionalidades.
    * `lombok`: Reduz código boilerplate (getters, setters, construtores) através de anotações.

* **Principais Dependências de Teste:**
    * `spring-boot-starter-test`: Pacote padrão para testes unitários e de integração com JUnit 5.
    * `spring-security-test`: Suporte para testar endpoints protegidos pelo Spring Security.
    * `selenium-java` & `webdrivermanager`: Indicam a presença de **testes de ponta a ponta (E2E)** que simulam a interação de um usuário real em um navegador.

* **Build:**
    * O `spring-boot-maven-plugin` é utilizado para empacotar a aplicação em um **JAR executável**, que contém todas as dependências necessárias para rodar de forma independente.

---

## 2. `application.properties` - Configuração da Aplicação

Este arquivo contém as configurações para o ambiente de **desenvolvimento**.

* **`server.port=8080`**: Define que a aplicação rodará na porta 8080 localmente.
* **`logging.level.*=DEBUG`**: Configura o nível de log para `DEBUG` em pacotes específicos, o que é ideal para desenvolvimento, pois exibe informações detalhadas sobre o que o Spring Security e o Spring Web estão fazendo.
* **`spring.data.mongodb.uri`**: A **Connection String** para se conectar ao banco de dados MongoDB.
* **`spring.thymeleaf.cache=false`**: Desabilita o cache do Thymeleaf em desenvolvimento, permitindo que alterações nos arquivos HTML sejam vistas sem a necessidade de reiniciar o servidor.
* **`spring.servlet.multipart.*`**: Configura os limites para upload de arquivos, como o tamanho máximo de 10MB por arquivo.
* **`app.upload.dir=uploads/images`**: Uma **propriedade customizada** que define o diretório local onde as imagens enviadas pelos usuários serão salvas.

> **Nota de Segurança e Boas Práticas:**
>
> A chave `spring.data.mongodb.uri` neste arquivo contém credenciais (usuário e senha). Para o ambiente de desenvolvimento atual, isso é funcional.
>
> No entanto, para ambientes de **produção** ou para tornar o repositório **público**, a prática recomendada é remover as credenciais do arquivo e carregá-las a partir de **Variáveis de Ambiente**. Isso evita a exposição de senhas no código-fonte.
>
> **Exemplo para o Futuro:**
> 1. No `application.properties`, a linha seria: `spring.data.mongodb.uri=${MONGODB_URI}`
> 2. Na plataforma de deploy (como Render) ou no sistema local, a variável de ambiente `MONGODB_URI` seria definida com o valor completo: `mongodb+srv://admin:senha_secreta@cluster...`

---

## 3. `Dockerfile` - Containerização com Docker

O `Dockerfile` é projetado para criar uma imagem de contêiner otimizada e segura para a aplicação, ideal para deploy em plataformas como a Render.

* **Build Multi-Estágio (Multi-Stage Build):**
    1.  **Estágio `build`:** Utiliza uma imagem completa do `maven:3.9.4-...-alpine` para compilar o código Java e gerar o arquivo `.jar` final. A cópia isolada do `pom.xml` no início (`COPY pom.xml .`) otimiza o cache do Docker, fazendo com que as dependências só sejam baixadas novamente se o `pom.xml` mudar.
    2.  **Estágio de Runtime:** Utiliza uma imagem JRE (Java Runtime Environment) muito mais leve (`eclipse-temurin:17-jre-alpine`). Apenas o `.jar` gerado no estágio anterior é copiado para cá, resultando em uma imagem final significativamente menor.

* **Práticas de Segurança:**
    * A imagem cria e utiliza um **usuário não-root** (`spring`) para rodar a aplicação, o que é uma prática de segurança fundamental para reduzir a superfície de ataque do contêiner.
    * As permissões do diretório de upload (`/tmp/uploads`) e do arquivo `app.jar` são explicitamente definidas para este usuário.

* **Configuração de Runtime:**
    * **`EXPOSE $PORT`**: Expõe a porta definida pela variável de ambiente `$PORT`, uma prática comum em plataformas de nuvem que gerenciam a porta dinamicamente.
    * **`ENV SPRING_PROFILES_ACTIVE=prod`**: Garante que a aplicação sempre rode com o perfil de `produção` dentro do contêiner.
    * **`ENV JAVA_OPTS`**: Define opções otimizadas para a JVM, controlando o uso de memória (`-Xmx512m`) e o Garbage Collector (`-XX:+UseG1GC`) para melhor performance.
    * **`ENTRYPOINT ["dumb-init", "--"]`**: Utiliza o `dumb-init` para iniciar a aplicação, o que melhora o gerenciamento de sinais do processo dentro do contêiner, garantindo que a aplicação possa ser parada de forma graciosa.