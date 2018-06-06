package info.diniz.pontointeligente.api.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import info.diniz.pontointeligente.api.entities.Empresa;
import info.diniz.pontointeligente.api.entities.Funcionario;
import info.diniz.pontointeligente.api.enums.PerfilEnum;
import info.diniz.pontointeligente.api.utils.PasswordUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class FuncionarioRepositoryTest {

	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	private static final String EMAIL = "email@email.com";
	private static final String CPF = "24291173474";
	
	@Before
	public void setUp() throws Exception {
		Empresa empresa = this.empresaRepository.save(obterDadosEmpresa());
		this.funcionarioRepository.save(obterDadosFuncionario(empresa));
	}
	
	@After
	public final void tearDown() {
		this.empresaRepository.deleteAll();
	}
	
	@Test
	public void testBuscarFuncionarioPorEmail() {
		Funcionario funcionario = this.funcionarioRepository.findByEmail(EMAIL);
		assertEquals(EMAIL, funcionario.getEmail());
	}
	
	@Test
	public void testBuscarFuncionarioPorCpf() {
		Funcionario funcionario = this.funcionarioRepository.findByCpf(CPF);
		assertEquals(CPF, funcionario.getCpf());
	}
	
	@Test
	public void testBuscarFuncionarioPorEmailECpf() {
		Funcionario funcionario = this.funcionarioRepository.findByCpfOrEmail(CPF, EMAIL);
		assertNotNull(funcionario);
	}
	
	@Test
	public void testBuscarFuncionarioPorEmailOuCpfParaEmailInvalido() {
		Funcionario funcionario = this.funcionarioRepository.findByCpfOrEmail(CPF, "email@invalido.com.br");
		assertNotNull(funcionario);
	}
	
	@Test
	public void testBuscarFuncionarioPorEmailECpfParaCpfInvalido() {
		Funcionario funcionario = this.funcionarioRepository.findByCpfOrEmail("12345678901", EMAIL);
		assertNotNull(funcionario);
	}
	
	private Funcionario obterDadosFuncionario(Empresa empresa) throws NoSuchAlgorithmException {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome("Fulano de Tal");
		funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
		funcionario.setSenha(PasswordUtils.geraBCrypt("123456"));
		funcionario.setCpf(CPF);
		funcionario.setEmail(EMAIL);
		funcionario.setEmpresa(empresa);
		funcionario.setQtdHorasAlmoco(1.0f);
		funcionario.setQtdHorasTrabalhoDia(8.0f);
		funcionario.setValorHora(BigDecimal.valueOf(50));
		return funcionario;
	}
	
	private Empresa obterDadosEmpresa() {
		Empresa empresa = new Empresa();
		empresa.setRazaoSocial("Empresa de exemplo");
		empresa.setCnpj("51463645000100");
		return empresa;
	}
}
