package br.com.alura.leilao.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;

class FinalizarLeilaoServiceTest {

	private FinalizarLeilaoService service;
	
	@Mock
	private LeilaoDao leilaoDao;
	
	@Mock
	private EnviadorDeEmails enviadorDeEmails;
	
	@SuppressWarnings("deprecation")
	@BeforeEach
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		this.service = new FinalizarLeilaoService(leilaoDao, enviadorDeEmails);
	}
	
	@Test
	void deveriaFinalizarUmLeilao() {
		List<Leilao> leiloes = leiloes();
		//Mockito qdo leilaodao buscar leiloes expirados
		Mockito.when(leilaoDao.buscarLeiloesExpirados())
		//me devolva essa lista
		.thenReturn(leiloes);
		//usando esse metodo
		service.finalizarLeiloesExpirados();
		
		Leilao leilao = leiloes.get(0);
		//verificar se o leilao foi marcado como fechado
		Assert.assertTrue(leilao.isFechado());
		//verificar se o lance vencedor é o de 900
		Assert.assertEquals(new BigDecimal("900"), 
				leilao.getLanceVencedor().getValor());
		//verificar se o dao foi chamado para salvar passando o leilao.
		Mockito.verify(leilaoDao).salvar(leilao);
	}
	
	//Testar envio do email
	@Test
	void deveriaEnviarEmailParaVencedorDoLeilao() {
		List<Leilao> leiloes = leiloes();
		
		Mockito.when(leilaoDao.buscarLeiloesExpirados())
		.thenReturn(leiloes);
		
		service.finalizarLeiloesExpirados();	
		
		Leilao leilao = leiloes.get(0);
		Lance lanceVencedor = leilao.getLanceVencedor();
		
		Mockito.verify(enviadorDeEmails)
		.enviarEmailVencedorLeilao(lanceVencedor);
	}
		
	//Testar Excepction
	@Test
	void naoDeveriaEnviarEmailParaVencedorDoLeilaoEmCasoDeErroAoEncerrarOLeilao() {
		List<Leilao> leiloes = leiloes();
		
		Mockito.when(leilaoDao.buscarLeiloesExpirados())
		.thenReturn(leiloes);
		
		Mockito.when(leilaoDao.salvar(Mockito.any()))
		.thenThrow(RuntimeException.class);
		
		try {
			service.finalizarLeiloesExpirados();	
			Mockito.verifyNoMoreInteractions(enviadorDeEmails);
		} catch (Exception e) {}
		
	}
	
	
	private List<Leilao> leiloes(){
		List<Leilao> lista = new ArrayList<>();
		Leilao leilao = new Leilao("Celular", 
				new BigDecimal("500"), 
				new Usuario("Fulano"));
		
		Lance primeiro = new Lance(new Usuario("Beltrano"),
				new BigDecimal("600"));
		Lance segundo = new Lance(new Usuario("Ciclano"),
				new BigDecimal("900"));
		
		leilao.propoe(primeiro);
		leilao.propoe(segundo);
		
		lista.add(leilao);
		
		return lista;
		
	}
}
