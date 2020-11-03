package edu.deu.seniorproject;

import edu.deu.seniorproject.nlp.Driver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {

	public static void main(String[] args) throws InterruptedException {
		driverMain();
		//htmlParseTest();
	}

	private static void driverMain() throws InterruptedException{
		Driver driverProgram = new Driver();
		driverProgram.start();
	}


	private static void htmlParseTest(){
		String text = "<html><body> <li>First Item</li><li>Second Item</li><li>Third Item</li> </body></html>";
		Document doc = Jsoup.parse(text);
		// This gets all list elements
		Elements elements = doc.body().select("li");
		for (Element element : elements) {
			System.out.println(element.text());
		}
	}
}
