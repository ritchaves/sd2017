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

	private static final String BASEBALL_BAT = "Baseball bat";
	private static final String BASEBALL = "Baseball";
	private static final String X1 = "X1";
	private static SupplierClient sp1;
	private static SupplierClient sp2;
	private static SupplierClient sp3;

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws InvalidItemId_Exception, SupplierClientException, BadProductId_Exception, BadProduct_Exception {
		
		mediatorClient.clear();
		
		{
			sp1 = new SupplierClient(mediatorClient.getWsURL());
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Baseball ball");
			product.setPrice(11);
			product.setQuantity(10);
			sp1.createProduct(product);
		}
		{
			sp2 = new SupplierClient(mediatorClient.getWsURL());
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc(BASEBALL_BAT);
			product.setPrice(69);
			product.setQuantity(20);
			sp2.createProduct(product);
		}
		{
			sp3 = new SupplierClient(mediatorClient.getWsURL());
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Baseball Over 9000");
			product.setPrice(9000);
			product.setQuantity(20);
			sp3.createProduct(product);
		}		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		mediatorClient.clear();
	}
	
	@Test
	public void searchItemsSucess() throws InvalidText_Exception {
		List<ItemView> Itemviewlist = mediatorClient.searchItems(BASEBALL);
		assertFalse(Itemviewlist.isEmpty());
		assertEquals(3, Itemviewlist.size());
		
		for (ItemView iv : Itemviewlist) {
			assertThat(iv.getDesc(), containsString(BASEBALL));
		}
	
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
	
	@Test(expected = InvalidItemId_Exception.class)
	public void searchItemsNullTest() throws InvalidText_Exception {
		mediatorClient.searchItems(null);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void searchItemsEmptyTest() throws InvalidText_Exception {
		mediatorClient.searchItems("");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void searchItemsWhiteSpaceTest() throws InvalidText_Exception {
		mediatorClient.searchItems(" ");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void searchItemsTabTest() throws InvalidText_Exception {
		mediatorClient.searchItems("\t");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void searchItemsNewLineTest() throws InvalidText_Exception {
		mediatorClient.searchItems("\n");
	}
}
	
	
	