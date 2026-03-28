Feature: Order API

  Background:
    * url baseUrl

  Scenario: Create order and fetch by id
    * def suffix = java.util.UUID.randomUUID() + ''
    * def customerEmail = 'order.' + suffix + '@test.com'

    Given path '/api/customers'
    And request { name: 'Order Customer', email: '#(customerEmail)' }
    When method post
    Then status 200
    * def customerId = response.id

    Given path '/api/orders'
    And request
      """
      {
        "customerId": #(customerId),
        "product": "Laptop",
        "quantity": 2,
        "price": 799.99,
        "orderDate": "2026-03-10"
      }
      """
    When method post
    Then status 200
    And match response.product == 'Laptop'
    * def orderId = response.id

    Given path '/api/orders', orderId
    When method get
    Then status 200
    And match response.id == orderId
    And match response.customerId == customerId

  Scenario: Get all orders and filter by date range
    Given path '/api/orders'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response == { content: '#[]', currentPage: '#number', pageSize: '#number', totalElements: '#number' }

    Given path '/api/orders/range'
    And param from = '2026-03-01'
    And param to = '2026-03-31'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response == { content: '#[]', currentPage: '#number', pageSize: '#number', totalElements: '#number' }

  Scenario: Invalid date range returns bad request
    Given path '/api/orders/range'
    And param from = '2026-03-31'
    And param to = '2026-03-01'
    When method get
    Then status 400

