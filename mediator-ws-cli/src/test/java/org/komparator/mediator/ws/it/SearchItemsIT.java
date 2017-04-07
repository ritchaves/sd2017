package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.*;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;


/**
 * Test suite
 */
public class SearchItemsIT extends BaseIT {

	private static final String X69 = "X69";
	private static final String BASEBALL_BAT = "Baseball bat";
	private static final String BASEBALL = "Baseball";
	private static final String X1 = "X1";
	private static SupplierClient sp1;
	private static SupplierClient sp2;
	private static SupplierClient sp3;

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws InvalidText_Exception, SupplierClientException, BadProductId_Exception, BadProduct_Exception {
		
		mediatorClient.clear();
		
		{
			sp1 = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Baseball ball");
			product.setPrice(20);
			product.setQuantity(10);
			sp1.createProduct(product);
		}
		{
			sp2 = new SupplierClient("http://localhost:8082/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X69);
			product.setDesc(BASEBALL_BAT);
			product.setPrice(15);
			product.setQuantity(20);
			sp2.createProduct(product);
		}
		{
			sp3 = new SupplierClient("http://localhost:8083/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId("X3");
			product.setDesc("Baseball Over 9000");
			product.setPrice(10);
			product.setQuantity(20);
			sp3.createProduct(product);
			
			ProductView product2 = new ProductView();
			product2.setId(X69);
			product2.setDesc("Baseball all the way");
			product2.setPrice(1);
			product2.setQuantity(20);
			sp3.createProduct(product2);
		}		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		mediatorClient.clear();
		sp1.clear();
		sp2.clear();
		sp3.clear();
	}
	
	@Test
	public void searchItemsSucess() throws InvalidText_Exception {
		List<ItemView> Itemviewlist = mediatorClient.searchItems(BASEBALL);
		assertFalse(Itemviewlist.isEmpty());
		assertEquals(4, Itemviewlist.size());
		
		for (ItemView iv : Itemviewlist) {
			assertThat(iv.getDesc(), containsString(BASEBALL));
		}
		
		ItemView firstitem = Itemviewlist.get(0);
		assertEquals(X1, firstitem.getItemId().getProductId());
		assertEquals(20, firstitem.getPrice());
		
		ItemView seconditem = Itemviewlist.get(0);
		assertEquals("X3", seconditem.getItemId().getProductId());
		assertEquals(10, seconditem.getPrice());
		
		ItemView thirditem = Itemviewlist.get(0);
		assertEquals(X69, thirditem.getItemId().getProductId());
		assertEquals(15, thirditem.getPrice());
		
	
	}
	
	@Test
	public void searchItemsNameWithSpacesTest() throws InvalidText_Exception {
		List<ItemView> Itemviewlist = mediatorClient.searchItems(BASEBALL_BAT);
		assertFalse(Itemviewlist.isEmpty());
		assertEquals(1, Itemviewlist.size());
		
		for (ItemView iv : Itemviewlist) {
			assertThat(iv.getDesc(), containsString(BASEBALL_BAT));
		}
	}
		
	@Test
	public void searchItemsCaseSensitiveTest() throws InvalidText_Exception {
		List<ItemView> iv = mediatorClient.searchItems("baseball");
		assertTrue(iv.isEmpty());
	}
	
	@Test
	public void searchItemsDontExist() throws InvalidText_Exception {
		List<ItemView> iv = mediatorClient.searchItems("YOLO");
		assertTrue(iv.isEmpty());
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void searchItemsNullTest() throws InvalidText_Exception {
		mediatorClient.searchItems(null);
	}

	@Test(expected = InvalidText_Exception.class)
	public void searchItemsEmptyTest() throws InvalidText_Exception {
		mediatorClient.searchItems("");
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void searchItemsWhiteSpaceTest() throws InvalidText_Exception {
		mediatorClient.searchItems(" ");
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void searchItemsTabTest() throws InvalidText_Exception {
		mediatorClient.searchItems("\t");
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void searchItemsNewLineTest() throws InvalidText_Exception {
		mediatorClient.searchItems("\n");
	}
}
	
	
	