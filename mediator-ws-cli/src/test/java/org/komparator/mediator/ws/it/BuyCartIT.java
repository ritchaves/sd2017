package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.ArrayList;
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
public class BuyCartIT extends BaseIT {

	private static final String DUCKY = "Ducky";
	private static final String CS3 = "CS3";
	private static final String X2 = "X2";
	private static final String X1 = "X1";
	private static SupplierClient sp1;
	private static SupplierClient sp2;
	private static SupplierClient sp3;
	private static ItemIdView itemidview = null;
	private static ItemIdView itemidview2 = null;
	private static ItemIdView itemidview3 = null;
	private static String ValidCCN = "4024007102923926";

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws InvalidItemId_Exception, SupplierClientException, BadProductId_Exception, BadProduct_Exception {
		
		mediatorClient.clear();
		
		{
			sp1 = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc(DUCKY);
			product.setPrice(11);
			product.setQuantity(10);
			sp1.createProduct(product);
			
			itemidview = new ItemIdView();
			itemidview.setProductId(X1);
			itemidview.setSupplierId("A57_Supplier1");	
		}
		{
			sp2 = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X2);
			product.setDesc("Duckywow");
			product.setPrice(5);
			product.setQuantity(20);
			sp2.createProduct(product);
			
			itemidview2 = new ItemIdView();
			itemidview2.setProductId(X2);
			itemidview2.setSupplierId("A57_Supplier2");
		}
		{
			sp3 = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Ducky much wow");
			product.setPrice(10);
			product.setQuantity(20);
			sp3.createProduct(product);
			
			itemidview3 = new ItemIdView();
			itemidview3.setProductId(X1);
			itemidview3.setSupplierId("A57_Supplier3");
		}		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		mediatorClient.clear();
	}
	
	@Test
	public void BuyCartSucess() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		mediatorClient.addToCart("SC3", itemidview, 1);		//price in order: 11 5 10 (* 1 3 2)
		mediatorClient.addToCart(CS3, itemidview2, 3);
		mediatorClient.addToCart(CS3, itemidview3, 2);
		
		ShoppingResultView toTest = mediatorClient.buyCart(CS3, ValidCCN);
		assertEquals(36,toTest.getTotalPrice());
		assertTrue(toTest.getDroppedItems().isEmpty());
		
		List<CartItemView> purchaseditems = toTest.getPurchasedItems();
		
		List<CartItemView> expecteditems = new ArrayList<CartItemView>();
		CartItemView toAdd = new CartItemView();
		ItemView iv1 = new ItemView();
		iv1.setDesc(DUCKY);
		iv1.setPrice(11);
		iv1.setItemId(itemidview);
		toAdd.setItem(iv1);
		toAdd.setQuantity(1);
		
		//assertEquals();
	}
	
	@Test
	public void BuyCartCaseSensitiveTest() throws InvalidItemId_Exception {
		//TODOList<ItemView> iv = mediatorClient.buyCart("x1");
		//assertTrue(iv.isEmpty());
	}
	
	@Test
	public void BuyCartDontExist() throws InvalidItemId_Exception {
		//TODOList<ItemView> iv = mediatorClient.buyCart("YOLO");
		//assertTrue(iv.isEmpty());
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void BuyCartNullTest() throws InvalidItemId_Exception {
		//TODOmediatorClient.buyCart(null);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void BuyCartEmptyTest() throws InvalidItemId_Exception {
		//TODOmediatorClient.buyCart("");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void BuyCartWhiteSpaceTest() throws InvalidItemId_Exception {
		//TODOmediatorClient.buyCart(" ");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void BuyCartTabTest() throws InvalidItemId_Exception {
		//TODOmediatorClient.buyCart("\t");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void BuyCartNewLineTest() throws InvalidItemId_Exception {
		//TODOmediatorClient.buyCart("\n");
	}
}
	
	
	