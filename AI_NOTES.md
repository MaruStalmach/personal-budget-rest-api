This project was developed with the help of AI, used as a development tool alongside 
the usual ones. 
This note describes where AI was involved to esnure transparency

The application design, the domain modelling, and the decisions about behaviour were mine; 
AI was used mainly to speed up test writing, infrastructure setup and debugging and documentation

## System prompt
    You are an expert software developer, proficient in all major programming languages 
    (Python, JavaScript, Go, Rust, Java, C++, SQL). Always provide the code solution 
    immediately, followed by brief explanations. Always follow SOLID principles, write DRY 
    code, and prioritize security. Always include comments only for complex logic. Always 
    include robust error handling and edge case checks. Always identify bugs and provide 
    a refactored, optimized version if the user provides code. Always use modern syntax 
    (e.g., Python 3.12+, ES6+).

## Tests prompt
The integration tests in src/main/java/.../integration were generated using AI.
Rather than it being a single-prompt approach, it was more of an iterative process.
The initial prompt:

    Given the following files and the task, write integration tests considering 
    I will have separate unit tests for services and entities. Make sure everything 
    is seamless and works well and is done according to modern standards."

after that adding exception handling and adding extra tests was done manually.

## Containerization in Docker
The Dockerfile, docker-compose.yml, .dockerignore, and .gitattributes were produced with AI assistance, 
along with the move to environment-variable-based configuration in application.properties (${VAR:default}) 
so the same config works for local runs and inside Docker

## README.md
The README.md file was generated using AI and then checked by running all the provided commands on another computer to ensure 
the instructions are clear and working

## Unit test fixes
For the unit tests I wrote myself, AI helped diagnose failures and suggested addition of some extra tests

## Verification
Everything created with the leverage of AI was checked by actually running it to confirm the behaviour:
- ./mvnw test for the test suite
- docker compose up --build for the full stack
- manual curl requests against every endpoint 
