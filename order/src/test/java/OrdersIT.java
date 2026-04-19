@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrdersIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrder() throws Exception {
        var payload = Map.of(
            "customerId", "CUST-001",
            "product",    "Widget",
            "quantity",   3,
            "price",      19.99,
            "orderDate",  "2026-04-19"
        );

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.customerId").value("CUST-001"))
            .andExpect(jsonPath("$.product").value("Widget"))
            .andExpect(jsonPath("$.quantity").value(3))
            .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        var invalid = Map.of("product", "Widget"); // missing required fields

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());
    }
}
