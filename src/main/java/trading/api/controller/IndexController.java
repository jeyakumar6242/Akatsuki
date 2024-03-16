package trading.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import trading.api.model.UserInput;

@Controller
public class IndexController {
	
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@PostMapping("/login")
	public String login(@ModelAttribute UserInput userInput) {
		System.out.println(userInput.toString());
		return "index";
	}

}
