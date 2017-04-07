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

	private static final String DUCKY_MUCH_WOW = "Ducky much wow";
	private static final String DUCKYWOW = "Duckywow";
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
	@Before
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
			sp2 = new SupplierClient("http://localhost:8082/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X2);
			product.setDesc(DUCKYWOW);
			product.setPrice(5);
			product.setQuantity(20);
			sp2.createProduct(product);
			
			itemidview2 = new ItemIdView();
			itemidview2.setProductId(X2);
			itemidview2.setSupplierId("A57_Supplier2");
		}
		{
			sp3 = new SupplierClient("http://localhost:8083/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc(DUCKY_MUCH_WOW);
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
		sp1.clear();
		sp2.clear();
		sp3.clear();
	}
	
	@Test
	public void BuyCartCompleteSucess() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		mediatorClient.addToCart(CS3, itemidview, 1);		//price in order: 11 5 10 (* 1 3 2)  11 15 20
		mediatorClient.addToCart(CS3, itemidview2, 3);
		mediatorClient.addToCart(CS3, itemidview3, 2);
		
		ShoppingResultView toTest = mediatorClient.buyCart(CS3, ValidCCN);
		assertEquals(46,toTest.getTotalPrice());
		assertTrue(toTest.getDroppedItems().isEmpty());
		assertEquals(Result.COMPLETE, toTest.getResult());
		
		List<CartItemView> purchaseditems = toTest.getPurchasedItems();
		List<CartItemView> expecteditems = new ArrayList<CartItemView>();
		
		CartItemView toAdd = new CartItemView();
		ItemView iv1 = new ItemView();
		iv1.setDesc(DUCKY);
		iv1.setPrice(11);
		iv1.setItemId(itemidview);
		toAdd.setItem(iv1);
		toAdd.setQuantity(1);
		
		CartItemView toAdd2 = new CartItemView();
		ItemView iv2 = new ItemView();
		iv2.setDesc(DUCKYWOW);
		iv2.setPrice(5);
		iv2.setItemId(itemidview2);
		toAdd2.setItem(iv2);
		toAdd2.setQuantity(3);
		
		CartItemView toAdd3 = new CartItemView();
		ItemView iv3 = new ItemView();
		iv3.setDesc(DUCKY_MUCH_WOW);
		iv3.setPrice(10);
		iv3.setItemId(itemidview3);
		toAdd3.setItem(iv3);
		toAdd3.setQuantity(2);
		
		expecteditems.add(toAdd);
		expecteditems.add(toAdd2);
		expecteditems.add(toAdd3);
		
		assertEquals(expecteditems, purchaseditems);
	}
	
	@Test
	public void BuyCartPartialSucess() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		mediatorClient.addToCart(CS3, itemidview, 1);		//price in order: 11 5 10 (* 1 3 2)
		mediatorClient.addToCart(CS3, itemidview2, 200);
		mediatorClient.addToCart(CS3, itemidview3, 2);
		
		ShoppingResultView toTest = mediatorClient.buyCart(CS3, ValidCCN);
		assertEquals(31,toTest.getTotalPrice());
		assertTrue(!toTest.getDroppedItems().isEmpty());
		assertEquals(Result.PARTIAL, toTest.getResult());
		
		List<CartItemView> purchaseditems = toTest.getPurchasedItems();
		List<CartItemView> expecteditems = new ArrayList<CartItemView>();
		List<CartItemView> droppeditems = new ArrayList<CartItemView>();
		
		CartItemView toAdd = new CartItemView();
		ItemView iv1 = new ItemView();
		iv1.setDesc(DUCKY);
		iv1.setPrice(11);
		iv1.setItemId(itemidview);
		toAdd.setItem(iv1);
		toAdd.setQuantity(1);
		
		CartItemView toDrop2 = new CartItemView();
		ItemView iv2 = new ItemView();
		iv2.setDesc(DUCKYWOW);
		iv2.setPrice(5);
		iv2.setItemId(itemidview2);
		toDrop2.setItem(iv2);
		toDrop2.setQuantity(200);
		
		CartItemView toAdd3 = new CartItemView();
		ItemView iv3 = new ItemView();
		iv3.setDesc(DUCKY_MUCH_WOW);
		iv3.setPrice(10);
		iv3.setItemId(itemidview3);
		toAdd3.setItem(iv3);
		toAdd3.setQuantity(2);
		
		expecteditems.add(toAdd);
		expecteditems.add(toAdd3);
		droppeditems.add(toDrop2);
	
		assertEquals(expecteditems, purchaseditems);
		assertEquals(droppeditems, toTest.getDroppedItems());
	}
	
	@Test
	public void BuyCartEmptySucess() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		mediatorClient.addToCart(CS3, itemidview, 500);		//price in order: 11 5 10 (* 1 3 2)
		mediatorClient.addToCart(CS3, itemidview2, 200);
		mediatorClient.addToCart(CS3, itemidview3, 500);
		
		ShoppingResultView toTest = mediatorClient.buyCart(CS3, ValidCCN);
		assertEquals(0,toTest.getTotalPrice());
		assertTrue(!toTest.getDroppedItems().isEmpty());
		assertTrue(toTest.getPurchasedItems().isEmpty());
		assertEquals(Result.EMPTY, toTest.getResult());
		
		List<CartItemView> expecteditems = new ArrayList<CartItemView>();
		
		CartItemView toAdd = new CartItemView();
		ItemView iv1 = new ItemView();
		iv1.setDesc(DUCKY);
		iv1.setPrice(11);
		iv1.setItemId(itemidview);
		toAdd.setItem(iv1);
		toAdd.setQuantity(500);
		
		CartItemView toAdd2 = new CartItemView();
		ItemView iv2 = new ItemView();
		iv2.setDesc(DUCKYWOW);
		iv2.setPrice(5);
		iv2.setItemId(itemidview2);
		toAdd2.setItem(iv2);
		toAdd2.setQuantity(200);
		
		CartItemView toAdd3 = new CartItemView();
		ItemView iv3 = new ItemView();
		iv3.setDesc(DUCKY_MUCH_WOW);
		iv3.setPrice(10);
		iv3.setItemId(itemidview3);
		toAdd3.setItem(iv3);
		toAdd3.setQuantity(500);
		
		expecteditems.add(toAdd);
		expecteditems.add(toAdd2);
		expecteditems.add(toAdd3);
		
		assertEquals(expecteditems, toTest.getDroppedItems());
	}
	
		
	@Test(expected = EmptyCart_Exception.class)
	public void BuyCartCaseSensitiveTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		mediatorClient.addToCart(CS3, itemidview, 1);
		mediatorClient.buyCart("cs3", ValidCCN);
	}
	
	@Test(expected = EmptyCart_Exception.class)
	public void BuyCartEmptyCartTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		mediatorClient.addToCart(CS3, itemidview, 1);
		mediatorClient.buyCart("CS4", ValidCCN);
	}
	
	@Test(expected = InvalidCreditCard_Exception.class)
	public void BuyCartInvalidCCTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		mediatorClient.addToCart(CS3, itemidview, 1);
		mediatorClient.buyCart(CS3, "5555555555555555");
	}
	
		
	//invalid cart id tests
	@Test(expected = InvalidCartId_Exception.class)
	public void BuyCartInvalidCart() throws InvalidItemId_Exception, EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart("HA A", ValidCCN);
	}
	
	@Test(expected = InvalidCartId_Exception.class)
	public void BuyCartNullTest() throws InvalidItemId_Exception, EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart(null, ValidCCN);
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void BuyCartEmptyTest() throws InvalidItemId_Exception, EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart("", ValidCCN);
	}
	
	@Test(expected = InvalidCartId_Exception.class)
	public void BuyCartWhiteSpaceTest() throws InvalidItemId_Exception, EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart(" ", ValidCCN);
	}
	
	@Test(expected = InvalidCartId_Exception.class)
	public void BuyCartTabTest() throws InvalidItemId_Exception, EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart("\t", ValidCCN);
	}
	
	@Test(expected = InvalidCartId_Exception.class)
	public void BuyCartNewLineTest() throws InvalidItemId_Exception, EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart("\n", ValidCCN);
	}
}
	
	
	