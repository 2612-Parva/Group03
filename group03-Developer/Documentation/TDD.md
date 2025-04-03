# Test-Driven Development Documentation

## Introduction

This document outlines the Test-Driven Development (TDD) approach used in the Dalhousie Marketplace project after March 3rd, 2025, when the team implemented TDD practices following lab instruction.

## TDD Process

The team followed the standard TDD cycle:

1. **Write a test** - Create a test that defines a function or improvements to a function
2. **Run the test and see it fail** - Verify that the test fails as expected (because the functionality doesn't exist yet)
3. **Write the code** - Implement the minimum amount of code needed to pass the test
4. **Run the test and see it pass** - Verify that the implementation code works as expected
5. **Refactor code** - Clean up the implementation code while ensuring tests still pass

## Key TDD Implementations

### Bidding System Implementation

| TDD Commit | Description | Code Commit | Description |
|------------|-------------|-------------|-------------|
| 5c9db2a61b11cd8377a7338bc96bfd645c4b0dc0 | TDD - Biding Service - Verify bid creation, status updates, and related operations | 96b3b656a646f47536116ba03bedd22a5a29e5bf | feat: Implement complete bidding functionality |
| 622a249c3fdcc55670377afb208ffa579a5acc74 | TDD - Create tests for bid and stats functionality | 1afd8a9e63998534dd8e6d4f66464b3a26870bb1 | Corresponding code for TDD tests |
| 3e643221d0f67d88b33b38b992c415ef1b457542 | Comment out test case | f5ed2b17537963a288c20d87282b0ee7211aef7f | feat: Implement complete bidding functionality |
| a66f5c4c54dec2b77300c105fa240fe74bffc1f4 | TDD Implementation | f670f9e90bd205e738816578f07a0eef019ef639 | feat: improve orders, add download reciepts, add order icon and button to entire app |

### Cart and Listing Service Implementation

| TDD Commit | Description | Code Commit | Description |
|------------|-------------|-------------|-------------|
| 488992a27295526704785970d7be882afdef234d | TDD - Add test for in-active listing in Cart Service and listing Service | 055447d0aeca959c1da2b0e4f03b280f06ebdb6a | Change api frontend api mapping from payment to order |
| 3e73c4a40ac7f3f94f91c5efb64b1b52eb26b3b8 | TDD - Add two new tests for stock update and listItem status | f11ce02621efd995a88fcec59068ee44a52fbf30 | Fix missing setters and getters for Message Model and improve message DTO |
| 3a8c8b216869783d011baaaa2920c775c8f26c5d | Move PaymentServiceTest to src'/test directory | 3a8c8b216869783d011baaaa2920c775c8f26c5d | Move PaymentServiceTest to src'/test directory |
| 54780452df0bb5079f8cb478053c70a797da973f | Refactor OrderService test to test directory | 96b3b656a646f47536116ba03bedd22a5a29e5bf | feat: Implement complete bidding functionality |

### Notification and User Preferences Testing

| TDD Commit | Description | Code Commit | Description |
|------------|-------------|-------------|-------------|
| b13fe503fdbbb68126ef7a9e1e1e259d72df64ae | TDD of User prefrence for notification | a43732dec613c93bf52fd1601842a039bb41d93a | Feat#13: notification for Items and user prefrence setting done |
| 55fcf83cc48cdaf68c673544001ce044f82e12aa | TDD of Notification | a43732dec613c93bf52fd1601842a039bb41d93a | Feat#13: notification for Items and user prefrence setting done |
| 5b0b59ed61f3c1f4f9c9626499fb81217cffef8f | modified test case to check pipeline | 3e8e285d78c1c655b807504f6a52fde1869a418c | Merged Developer branch into ISSUE13_NOTIFICATION |

### Review Service Implementation

| TDD Commit | Description | Code Commit | Description |
|------------|-------------|-------------|-------------|
| 669d8a10f125f07eaccc3a94925fecdeadeee5b9 | TDD have been done for review service layer | 707d69a5cc0b6aaab7b499a6d54bbc0858b8cf2e | Review feature fully implemented |
| ad7947722a90cfc61cda24cd23fb09f0de48cd2e | feat: Review backend implemented | 5ca9339e52516651eeeb6057071be5fcaaa3bd45 | Review feature fully implemented |

### Message Service Testing

| TDD Commit | Description | Code Commit | Description |
|------------|-------------|-------------|-------------|
| 4eccca142659efea0e698d985464532702e77222 | Add MessageServiceTest.java | 811463f359ad773e3f0f1975aafe776cbee942c1 | Build backend API for the chat function |
| 6615acd9d20ba36db783b052eedd72053f96fbcb | Add MessageRepositoryTest.java | 811463f359ad773e3f0f1975aafe776cbee942c1 | Build backend API for the chat function |
| a872a35ed79e4c71fa4449dea7113fc3f6fbd6b6 | Add MessageTest.java | 811463f359ad773e3f0f1975aafe776cbee942c1 | Build backend API for the chat function |
| ba3384d74203f6f9e2af4b7452a219a550d51a89 | Add MessageControllerTest.java | 811463f359ad773e3f0f1975aafe776cbee942c1 | Build backend API for the chat function |
| 071b457c14ed54a2f88d71e22dae7cb6ea27d47e | update the message service test | 4a7aa846f697f11589f380c521a9de2d94987d4a | add listing ID to in the message service |

## Chronological TDD Implementation Timeline

After learning about TDD principles in the lab on March 3rd, 2025, the team began implementing TDD practices across various components:

| Date | TDD Activity | Developer |
|------|--------------|-----------|
| Mar 4, 2025 | Created initial Message service tests | Pratham Bhavsar |
| Mar 12, 2025 | Updated test organization and package structure | Pratham Bhavsar |
| Mar 17, 2025 | Implemented notification and user preference TDD | Kriti Jodhani |
| Mar 17, 2025 | Implemented review service layer TDD | Rafiqul Islam |
| Mar 20, 2025 | Added cart and listing service tests | Temidire Dimowo |
| Mar 21, 2025 | Established bidding service tests | Temidire Dimowo |
| Mar 23, 2025 | Expanded bidding and stats functionality tests | Temidire Dimowo |

## Testing Technologies

The project uses the following testing frameworks and technologies:

- **JUnit 5**: Primary testing framework for unit tests
- **Mockito**: For mocking dependencies in unit tests
- **MockMvc**: For testing Spring MVC controllers
- **AssertJ**: For fluent assertions in tests

## Test Coverage

The team aimed for and maintained a test coverage of:
- **Service Layer**: >85% coverage
- **Controller Layer**: >75% coverage
- **Repository Layer**: >70% coverage

## TDD Benefits Realized

Implementing TDD in our development process yielded several benefits:

1. **Clearer Requirements**: Writing tests first forced clarification of requirements before implementation
2. **Better Design**: Code written to be testable had cleaner interfaces and better separation of concerns
3. **Regression Prevention**: Comprehensive test suites alerted us to regressions when changing features
4. **Documentation**: Tests served as executable documentation of expected system behavior
5. **Confidence**: The team gained confidence in making changes knowing tests would catch issues
