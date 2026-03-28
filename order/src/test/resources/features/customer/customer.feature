Feature: Customer API

  Background:
    * url baseUrl

  Scenario: Create, fetch and delete a customer
    * def suffix = java.util.UUID.randomUUID() + ''
    Given path '/api/customers'
    And request { name: 'Karate User', email: '#("karate." + suffix + "@test.com")' }
    When method post
    Then status 200
    And match response == { id: '#number', name: 'Karate User', email: '#string', totalOrders: '#number' }
    * def customerId = response.id

    Given path '/api/customers', customerId
    When method get
    Then status 200
    And match response.id == customerId

    Given path '/api/customers', customerId
    When method delete
    Then status 204

    Given path '/api/customers', customerId
    When method get
    Then status 404

  Scenario: Validation error when creating invalid customer
    Given path '/api/customers'
    And request { name: '', email: 'not-valid-email' }
    When method post
    Then status 400
    And match response.error == 'Validation failed'
    And match response.fieldErrors.name == 'Name is required'

