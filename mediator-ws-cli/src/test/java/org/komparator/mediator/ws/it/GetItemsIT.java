package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
public class GetItemsIT extends BaseIT {

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
			product.setDesc("Baseball bat");
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
	public void getItemsSucess() throws InvalidItemId_Exception {
		List<ItemView> Itemviewlist = mediatorClient.getItems(X1);
		assertFalse(Itemviewlist.isEmpty());
		assertEquals(3, Itemviewlist.size());
		
		for (ItemView iv : Itemviewlist) {
			assertEquals(X1, iv.getItemId().getProductId());
			assertThat(iv.getDesc(), containsString("Baseball"));
		}
		
		ItemView firstitem = Itemviewlist.get(0);
		assertEquals("sp1", firstitem.getItemId().getSupplierId());
		assertEquals(10, firstitem.getPrice());
		
		ItemView seconditem = Itemviewlist.get(1);
		assertEquals("sp2", seconditem.getItemId().getSupplierId());
		assertEquals(69, seconditem.getPrice());
		
		ItemView thirditem = Itemviewlist.get(2);
		assertEquals("sp3", thirditem.getItemId().getSupplierId());
		assertEquals(9000, thirditem.getPrice());		
	}
	
	@Test
	public void getItemsCaseSensitiveTest() throws InvalidItemId_Exception {
		List<ItemView> iv = mediatorClient.getItems("x1");
		assertTrue(iv.isEmpty());
	}
	
	@Test
	public void getItemsDontExist() throws InvalidItemId_Exception {
		List<ItemView> iv = mediatorClient.getItems("YOLO");
		assertTrue(iv.isEmpty());
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsNullTest() throws InvalidItemId_Exception {
		mediatorClient.getItems(null);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsEmptyTest() throws InvalidItemId_Exception {
		mediatorClient.getItems("");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsWhiteSpaceTest() throws InvalidItemId_Exception {
		mediatorClient.getItems(" ");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsTabTest() throws InvalidItemId_Exception {
		mediatorClient.getItems("\t");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsNewLineTest() throws InvalidItemId_Exception {
		mediatorClient.getItems("\n");
	}
}
	
	
	