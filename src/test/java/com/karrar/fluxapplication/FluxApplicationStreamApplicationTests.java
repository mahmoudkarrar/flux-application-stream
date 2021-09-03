package com.karrar.fluxapplication;

import com.karrar.fluxapplication.service.KinesisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class FluxApplicationStreamApplicationTests {

	@MockBean
	KinesisService kinesisService;
	@Test
	void contextLoads() {
	}

}
