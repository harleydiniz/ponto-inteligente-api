package info.diniz.pontointeligente.api.controllers;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import info.diniz.pontointeligente.api.dtos.CadastroPfDto;
import info.diniz.pontointeligente.api.entities.Empresa;
import info.diniz.pontointeligente.api.entities.Funcionario;
import info.diniz.pontointeligente.api.enums.PerfilEnum;
import info.diniz.pontointeligente.api.response.Response;
import info.diniz.pontointeligente.api.services.EmpresaService;
import info.diniz.pontointeligente.api.services.FuncionarioService;
import info.diniz.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/cadastrar-pf")
@CrossOrigin(origins = "*")
public class CadastroPFController {

	private static final Logger log = LoggerFactory.getLogger(CadastroPJController.class);
	
	@Autowired
	private EmpresaService empresaService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	public CadastroPFController() {
	}
	
	/**
	 * Cadastra um funcionário pessoa física no sistema
	 * 
	 * @param cadastroPfDto
	 * @param result
	 * @return ResponseEntity<Response<CadastroPfDto>>
	 * @throws NoSuchAlgorithmException
	 */
	@PostMapping
	public ResponseEntity<Response<CadastroPfDto>> cadastrar(@Valid @RequestBody CadastroPfDto cadastroPfDto, 
			BindingResult result) throws NoSuchAlgorithmException {
		
		log.info("Cadastrando PF: {}", cadastroPfDto.toString());
		
		Response<CadastroPfDto> response = new Response<CadastroPfDto>();
		
		validarDadosExistentes(cadastroPfDto, result);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPfDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro validando dados de cadastro PF: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPfDto.getCnpj());
		empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
		this.funcionarioService.persistir(funcionario);
		
		response.setData(this.converterCadastroPFDto(funcionario));
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Verifica se a empresa está cadastrada e se o funcionário não existe na base de dados.
	 * 
	 * @param cadastroPfDto
	 * @param result
	 */
	private void validarDadosExistentes(CadastroPfDto cadastroPfDto, BindingResult result) {
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPfDto.getCnpj());
		if (!empresa.isPresent()) {
			result.addError(new ObjectError("empresa", "Empresa não cadastrada."));
		}
		
		this.funcionarioService.buscarPorCpf(cadastroPfDto.getCpf())
			.ifPresent(func -> result.addError(new ObjectError("funcionario", "CPF já existente")));
		
		this.funcionarioService.buscarPorEmail(cadastroPfDto.getEmail())
			.ifPresent(func -> result.addError(new ObjectError("funcionario", "Email já existente")));
	}
	
	/**
	 * Converte os dados do DTO para funcionário.
	 * 
	 * @param cadastroPfDto
	 * @param result
	 * @return Funcioario
	 * @throws NoSuchAlgorithmException
	 */
	private Funcionario converterDtoParaFuncionario(CadastroPfDto cadastroPfDto, BindingResult result)
		throws NoSuchAlgorithmException {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(cadastroPfDto.getNome());
		funcionario.setEmail(cadastroPfDto.getEmail());
		funcionario.setCpf(cadastroPfDto.getCpf());
		funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
		funcionario.setSenha(PasswordUtils.geraBCrypt(cadastroPfDto.getSenha()));
		cadastroPfDto.getQtdHorasAlmoco()
			.ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		cadastroPfDto.getQtdHorasTrabalhoDia()
			.ifPresent(qtdHorasTrabalhoDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabalhoDia)));
		cadastroPfDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		return funcionario;
	}
	
	/**
	 * Popula o DTO de cadastro com os dados do funcionário e empresa.
	 * 
	 * @param funcionario
	 * @return CadastroPfDto
	 */
	private CadastroPfDto converterCadastroPFDto(Funcionario funcionario) {
		CadastroPfDto dto = new CadastroPfDto();
		dto.setId(funcionario.getId());
		dto.setNome(funcionario.getNome());
		dto.setEmail(funcionario.getEmail());
		dto.setCpf(funcionario.getCpf());
		dto.setCnpj(funcionario.getEmpresa().getCnpj());
		funcionario.getQtdHorasAlmocoOpt().ifPresent(qtdHorasAlmoco -> dto
				.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(
				qtdHorasTrabDia -> dto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabDia))));
		funcionario.getValorHoraOpt()
			.ifPresent(valorHora -> dto.setValorHora(Optional.of(valorHora.toString())));
		return dto;
	}
}
