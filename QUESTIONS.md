# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt 
I would like to write resources classes manually, because it gives me more control over the code and allows me to write more concise and readable code. I can also use annotations to generate the OpenAPI documentation, which is a nice bonus. 
whenever adding new endpoints to existing resources is has with openapi yaml file, it is more difficult to maintain the yaml file and keep it in sync with the code. It also requires more boilerplate code to generate the OpenAPI documentation.
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
    I would prioritize unit tests for the core business logic, as they are fast to run and can catch issues early in the development process. I would also focus on integration tests for critical paths, such as database interactions and API endpoints, to ensure that the components work together correctly.
     then focus on the concurrecy related tests, as they are more complex and can reveal issues that may not be caught by unit tests. I would use parameterized tests to cover a wide range of input scenarios without having to write separate test cases for each one.
```
