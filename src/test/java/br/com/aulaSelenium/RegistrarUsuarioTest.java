package br.com.aulaSelenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Test Case 1: Register User
 *
 * Classes de equivalência:
 *  Nome   -> (V1) preenchido               | (I1) vazio
 *  E-mail -> (V2) válido e ainda não usado | (I2) sem '@' | (I3) já cadastrado
 *  Senha  -> (V3) preenchida               | (I4) vazia (etapa 9)
 *
 * Valores-limite do nome: 1 caractere (mínimo), nome típico, 50 caracteres (longo),
 * além de nome com acentos/espaço (caracteres especiais válidos).
 */
public class RegistrarUsuarioTest extends BaseTest {

	private static final By NEW_USER_TITULO = By.xpath("//h2[normalize-space()='New User Signup!']");
	private static final By SIGNUP_NOME = By.cssSelector("input[data-qa='signup-name']");
	private static final By SIGNUP_EMAIL = By.cssSelector("input[data-qa='signup-email']");
	private static final By BOTAO_SIGNUP = By.cssSelector("button[data-qa='signup-button']");
	private static final By TITULO_INFO_CONTA = By.xpath("//h2[contains(@class,'title')]/b");
	private static final By BOTAO_CRIAR_CONTA = By.cssSelector("button[data-qa='create-account']");
	private static final By CONTA_CRIADA = By.cssSelector("h2[data-qa='account-created']");
	private static final By BOTAO_CONTINUAR = By.cssSelector("a[data-qa='continue-button']");
	private static final By LOGADO_COMO = By.xpath("//a[contains(.,'Logged in as')]");
	private static final By BOTAO_DELETAR = By.cssSelector("a[href='/delete_account']");
	private static final By CONTA_DELETADA = By.cssSelector("h2[data-qa='account-deleted']");
	private static final By BOTAO_LOGOUT = By.cssSelector("a[href='/logout']");
	private static final By MSG_EMAIL_EXISTE = By.xpath("//p[normalize-space()='Email Address already exist!']");

	private static final String SENHA = "Senha@123";

	// ------------------------------------------------------------------
	// Passos reaproveitados
	// ------------------------------------------------------------------

	/** Passos 3 a 7: home, Signup / Login, preencher nome e e-mail, clicar Signup. */
	private void iniciarCadastro(String nome, String email) {
		verificarHomeVisivel();                                   // 3
		clicarSignupLogin();                                      // 4
		assertTrue(visivel(NEW_USER_TITULO).isDisplayed());       // 5
		visivel(SIGNUP_NOME).sendKeys(nome);                      // 6
		driver.findElement(SIGNUP_EMAIL).sendKeys(email);
		clicar(BOTAO_SIGNUP);                                     // 7
	}

	/** Passo 8 */
	private void verificarTelaInformacoesConta() {
		assertEquals("ENTER ACCOUNT INFORMATION", visivel(TITULO_INFO_CONTA).getText().trim().toUpperCase());
	}

	/** Passos 9 a 12: preenche o formulário de detalhes da conta. */
	private void preencherDetalhes(String senha) {
		clicar(By.id("id_gender1"));                              // 9 - Title (Mr.)
		// Name e Email já vêm preenchidos da etapa anterior
		driver.findElement(By.id("password")).sendKeys(senha);
		new Select(driver.findElement(By.id("days"))).selectByValue("16");
		new Select(driver.findElement(By.id("months"))).selectByValue("6");
		new Select(driver.findElement(By.id("years"))).selectByValue("1999");

		clicar(By.id("newsletter"));                              // 10
		clicar(By.id("optin"));                                   // 11

		driver.findElement(By.id("first_name")).sendKeys("Joao"); // 12
		driver.findElement(By.id("last_name")).sendKeys("Silveira");
		driver.findElement(By.id("company")).sendKeys("Empresa Teste");
		driver.findElement(By.id("address1")).sendKeys("Rua das Flores, 100");
		driver.findElement(By.id("address2")).sendKeys("Apto 201");
		new Select(driver.findElement(By.id("country"))).selectByVisibleText("Canada");
		driver.findElement(By.id("state")).sendKeys("Ontario");
		driver.findElement(By.id("city")).sendKeys("Toronto");
		driver.findElement(By.id("zipcode")).sendKeys("M5V2T6");
		driver.findElement(By.id("mobile_number")).sendKeys("5521999999999");
	}

	/** Passos 13 a 16: cria a conta e confirma que o usuário ficou logado. */
	private void criarContaEVerificarLogin(String nome) {
		clicar(BOTAO_CRIAR_CONTA);                                // 13
		assertEquals("ACCOUNT CREATED!", visivel(CONTA_CRIADA).getText().trim().toUpperCase()); // 14
		clicar(BOTAO_CONTINUAR);                                  // 15
		assertTrue(visivel(LOGADO_COMO).getText().contains("Logged in as " + nome)); // 16
	}

	/** Passos 17 e 18: exclui a conta (limpeza do ambiente). */
	private void deletarConta() {
		clicar(BOTAO_DELETAR);                                    // 17
		assertEquals("ACCOUNT DELETED!", visivel(CONTA_DELETADA).getText().trim().toUpperCase()); // 18
		clicar(BOTAO_CONTINUAR);
	}

	// ------------------------------------------------------------------
	// Casos válidos: fluxo completo do Test Case 1
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "{0}")
	@DisplayName("Registrar usuário com dados válidos")
	@CsvSource(delimiter = '|', value = {
		"CT-R01 nome típico                          | Maria Silva",
		"CT-R02 limite: nome com 1 caractere         | A",
		"CT-R03 limite: nome longo (50 caracteres)   | NomeComCinquentaCaracteresParaTesteDeLimiteXYZabcd",
		"CT-R04 nome com acentos e espaço            | João Conceição"
	})
	public void registrarUsuarioValido(String cenario, String nome) {
		iniciarCadastro(nome, emailUnico());
		verificarTelaInformacoesConta();
		preencherDetalhes(SENHA);
		criarContaEVerificarLogin(nome);
		deletarConta();
	}

	// ------------------------------------------------------------------
	// Casos inválidos na etapa "New User Signup!"
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "{0}")
	@DisplayName("Signup com entrada inválida é bloqueado pelo navegador")
	@CsvSource(delimiter = '|', value = {
		"CT-R05 nome vazio (limite: 0 caracteres) | ''                 | EMAIL_UNICO      | nome",
		"CT-R06 e-mail sem '@'                    | Maria Silva        | mariaexemplo.com | email"
	})
	public void signupComEntradaInvalida(String cenario, String nome, String email, String campoInvalido) {
		if ("EMAIL_UNICO".equals(email)) {
			email = emailUnico();
		}
		iniciarCadastro(nome, email);

		WebElement campo = driver.findElement("nome".equals(campoInvalido) ? SIGNUP_NOME : SIGNUP_EMAIL);
		assertFalse(mensagemValidacaoHtml5(campo).isEmpty(), cenario + " - deveria haver validação no campo");
		assertTrue(driver.getCurrentUrl().contains("/login"), cenario + " - não deveria avançar para o cadastro");
	}

	@Test
	@DisplayName("CT-R07 e-mail já cadastrado exibe 'Email Address already exist!'")
	public void signupComEmailJaCadastrado() {
		String nome = "Usuario Duplicado";
		String email = emailUnico();

		// Pré-condição: cria uma conta com o e-mail e faz logout
		iniciarCadastro(nome, email);
		verificarTelaInformacoesConta();
		preencherDetalhes(SENHA);
		criarContaEVerificarLogin(nome);
		clicar(BOTAO_LOGOUT);

		// Tenta cadastrar de novo com o mesmo e-mail
		assertTrue(visivel(NEW_USER_TITULO).isDisplayed());
		visivel(SIGNUP_NOME).sendKeys(nome);
		driver.findElement(SIGNUP_EMAIL).sendKeys(email);
		clicar(BOTAO_SIGNUP);
		assertTrue(visivel(MSG_EMAIL_EXISTE).isDisplayed());

		// Limpeza: entra na conta criada e a exclui
		visivel(By.cssSelector("input[data-qa='login-email']")).sendKeys(email);
		driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys(SENHA);
		clicar(By.cssSelector("button[data-qa='login-button']"));
		deletarConta();
	}

	@Test
	@DisplayName("CT-R08 senha vazia na etapa 9 impede a criação da conta")
	public void cadastroComSenhaVazia() {
		iniciarCadastro("Maria Silva", emailUnico());
		verificarTelaInformacoesConta();
		preencherDetalhes("");                                    // limite: senha com 0 caracteres
		clicar(BOTAO_CRIAR_CONTA);

		WebElement senha = driver.findElement(By.id("password"));
		assertFalse(mensagemValidacaoHtml5(senha).isEmpty(), "Campo senha deveria acusar obrigatoriedade");
		assertTrue(driver.findElements(CONTA_CRIADA).isEmpty(), "A conta não deveria ser criada");
		verificarTelaInformacoesConta();
	}
}
