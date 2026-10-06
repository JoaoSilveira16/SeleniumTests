package br.com.aulaSelenium;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Test Case 3: Login User with incorrect email and password
 *
 * Classes de equivalência:
 *  E-mail -> (V1) formato válido, não cadastrado | (I1) vazio | (I2) sem '@' | (I3) sem parte local
 *  Senha  -> (V2) preenchida, incorreta          | (I4) vazia
 *
 * Valores-limite:
 *  E-mail: menor formato aceito pelo navegador ("a@b"); parte local com 64 caracteres (máx. RFC 5321)
 *  Senha : 1 caractere (mínimo não vazio) e 256 caracteres (muito longa)
 *
 * Entradas que passam pela validação do navegador chegam ao servidor e devem
 * exibir "Your email or password is incorrect!". Entradas inválidas são barradas
 * pela validação HTML5 do campo (required / type=email) e o formulário nem é enviado.
 */
public class LoginIncorretoTest extends BaseTest {

	private static final By LOGIN_TITULO = By.xpath("//h2[normalize-space()='Login to your account']");
	private static final By CAMPO_EMAIL = By.cssSelector("input[data-qa='login-email']");
	private static final By CAMPO_SENHA = By.cssSelector("input[data-qa='login-password']");
	private static final By BOTAO_LOGIN = By.cssSelector("button[data-qa='login-button']");
	private static final By MSG_ERRO = By.xpath("//p[normalize-space()='Your email or password is incorrect!']");

	private static final String SENHA_256 = "x".repeat(256);
	private static final String LOCAL_64 = "a".repeat(64);

	/** Passos 3 a 7 do Test Case 3. */
	private void tentarLogin(String email, String senha) {
		verificarHomeVisivel();                               // 3
		clicarSignupLogin();                                  // 4
		assertTrue(visivel(LOGIN_TITULO).isDisplayed());      // 5
		visivel(CAMPO_EMAIL).sendKeys(email);                 // 6
		driver.findElement(CAMPO_SENHA).sendKeys(senha);
		clicar(BOTAO_LOGIN);                                  // 7
	}

	// ------------------------------------------------------------------
	// Entradas que chegam ao servidor -> mensagem de erro deve aparecer
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "{0}")
	@DisplayName("Login com credenciais incorretas exibe mensagem de erro")
	@CsvSource(delimiter = '|', value = {
		"CT-L01 e-mail válido não cadastrado + senha comum      | naoexiste_2026@exemplo.com  | Senha@123",
		"CT-L02 limite: menor e-mail aceito (a@b) + senha 1 char | a@b                         | 1",
		"CT-L03 limite: senha com 256 caracteres                | naoexiste_2026@exemplo.com  | SENHA_256",
		"CT-L04 limite: parte local do e-mail com 64 caracteres | LOCAL_64@exemplo.com        | Senha@123",
		"CT-L05 e-mail com caracteres especiais (+ e .com.br)   | usuario+qa@exemplo.com.br   | Senha@123"
	})
	public void loginIncorretoExibeErro(String cenario, String email, String senha) {
		email = email.replace("LOCAL_64", LOCAL_64);
		senha = senha.replace("SENHA_256", SENHA_256);

		tentarLogin(email, senha);

		// 8. Verify error 'Your email or password is incorrect!' is visible
		assertTrue(visivel(MSG_ERRO).isDisplayed(), cenario);
	}

	// ------------------------------------------------------------------
	// Entradas inválidas -> barradas pela validação do navegador
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "{0}")
	@DisplayName("Login com entrada inválida é bloqueado antes de enviar")
	@CsvSource(delimiter = '|', emptyValue = "", value = {
		"CT-L06 e-mail vazio                | ''                    | Senha@123 | email",
		"CT-L07 e-mail sem '@'              | usuarioexemplo.com    | Senha@123 | email",
		"CT-L08 e-mail sem parte local      | @exemplo.com          | Senha@123 | email",
		"CT-L09 senha vazia (limite: 0)     | naoexiste@exemplo.com | ''        | senha"
	})
	public void loginComEntradaInvalidaNaoEnvia(String cenario, String email, String senha, String campoInvalido) {
		tentarLogin(email, senha);

		WebElement campo = driver.findElement("email".equals(campoInvalido) ? CAMPO_EMAIL : CAMPO_SENHA);

		// O navegador mostra a mensagem de validação e o formulário não é enviado
		assertFalse(mensagemValidacaoHtml5(campo).isEmpty(), cenario + " - deveria haver validação no campo");
		assertTrue(driver.getCurrentUrl().contains("/login"), cenario + " - deveria continuar na página de login");
		assertTrue(driver.findElements(MSG_ERRO).isEmpty(), cenario + " - não deveria chegar ao servidor");
	}
}
