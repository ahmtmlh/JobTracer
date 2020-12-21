package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.model.dto.CVDTO;
import edu.deu.resumeie.service.service.ResumeMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/rest")
public class ResumeMatchingRestController {
	
	@Autowired
	private ResumeMatchingService resumeMatchingService;

	@PostMapping("/resumeInfo")
	public ResponseEntity<?> getNew(@RequestBody @Valid CVDTO cv){

		//  Convert CVDTO to CV
		//Burada ozgecmis nesnesi oluşturulacak, city yerine ozgecmis parametre olarak gelecek.
		System.out.println("check");

		return ResponseEntity.notFound().build();
	}
	
}
