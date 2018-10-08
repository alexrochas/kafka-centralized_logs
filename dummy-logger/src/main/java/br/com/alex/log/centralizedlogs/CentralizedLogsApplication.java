package br.com.alex.log.centralizedlogs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@SpringBootApplication
public class CentralizedLogsApplication {
	public static void main(String[] args) {
		SpringApplication.run(CentralizedLogsApplication.class, args);
		Timer timer = new Timer();
		timer.schedule(new SayHello(), 0, 5000);
	}
}

class SayHello extends TimerTask {
	private Logger logger = Logger.getGlobal();

    public void run() {
       logger.info("Log this if you dare!");
    }
}
