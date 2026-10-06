package br.com.aulaSelenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Classe base com o ciclo de vida do driver (mesmo modelo do GoogleTest)
 * e métodos auxiliares reaproveitados pelos testes.
 */
public abstract class BaseTest {

	protected static final String URL = "https://automationexercise.com";

	protected WebDriver driver;
	protected WebDriverWait wait;

	@BeforeEach
	public void createDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		// options.addArguments("--headless=new"); // descomente para rodar sem abrir janela

		// 1. Launch browser
		driver = WebDriverManager.chromedriver().capabilities(options).create();
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		// 2. Navigate to url
		driver.get(URL);
	}

	@AfterEach
	public void quitDriver() {
		if (driver != null) {
			driver.quit();
		}
	}

	// ---------------- Métodos auxiliares ----------------

	/** 3. Verify that home page is visible successfully */
	protected void verificarHomeVisivel() {
		assertEquals("Automation Exercise", driver.getTitle());
		assertTrue(driver.findElement(By.cssSelector("img[alt='Website for automation practice']")).isDisplayed());
	}

	/** 4. Click on 'Signup / Login' button */
	protected void clicarSignupLogin() {
		clicar(By.cssSelector("a[href='/login']"));
		wait.until(ExpectedConditions.urlContains("/login"));
	}

	/** Localiza um elemento aguardando que fique visível. */
	protected WebElement visivel(By by) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}

	/**
	 * Clica via JavaScript. O site exibe anúncios (iframes do Google Ads)
	 * que às vezes ficam por cima dos botões e causam
	 * ElementClickInterceptedException num clique normal.
	 */
	protected void clicar(By by) {
		WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(by));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
		fecharAnuncioSeAparecer();
	}

	/** Se o anúncio de tela cheia (#google_vignette) abrir, recarrega a página sem ele. */
	protected void fecharAnuncioSeAparecer() {
		if (driver.getCurrentUrl().contains("google_vignette")) {
			driver.navigate().to(driver.getCurrentUrl().replace("#google_vignette", ""));
		}
	}

	/** Mensagem de validação nativa do HTML5 (ex.: campo required ou e-mail sem '@'). */
	protected String mensagemValidacaoHtml5(WebElement campo) {
		return (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", campo);
	}

	/** Gera um e-mail único para não colidir com contas já existentes no site. */
	protected String emailUnico() {
		return "teste" + System.currentTimeMillis() + "@exemplo.com";
	}
}
