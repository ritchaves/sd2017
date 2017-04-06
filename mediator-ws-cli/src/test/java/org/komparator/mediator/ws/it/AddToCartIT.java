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
public class AddToCartIT extends BaseIT {

	private static final String BASEBALL_BAT = "Baseball bat";
	private static final String BASEBALL = "Baseball";
	private static final String X1 = "X1";
	private static SupplierClient sp1;
	private static SupplierClient sp2;
	private static SupplierClient sp3;
	static ItemIdView itemidview = null;
	static ItemIdView itemidview2 = null;
	static ItemIdView itemidview3 = null;


	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws InvalidItemId_Exception, SupplierClientException, BadProductId_Exception, BadProduct_Exception {
		
		mediatorClient.clear();
		sp1.clear();
		sp2.clear();
		sp3.clear();
		//Cart c = new Cart();
		
		
		{
			sp1 = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Baseball ball");
			product.setPrice(5);
			product.setQuantity(10);
			sp1.createProduct(product);
			
			itemidview = new ItemIdView();
			itemidview.setProductId(X1);
			itemidview.setSupplierId("81");
		}
		{
			sp2 = new SupplierClient("http://localhost:8082/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc(BASEBALL_BAT);
			product.setPrice(15);
			product.setQuantity(20);
			sp2.createProduct(product);
			
			itemidview2 = new ItemIdView();
			itemidview2.setProductId(X1);
			itemidview2.setSupplierId("82");
		}
		{
			sp3 = new SupplierClient("http://localhost:8083/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Baseball Over 9000");
			product.setPrice(10);
			product.setQuantity(20);
			sp3.createProduct(product);
			
			itemidview3 = new ItemIdView();
			itemidview3.setProductId(X1);
			itemidview3.setSupplierId("82");
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
	public void addToCartSucess() {
		//mediatorClient.addToCart(cart.getCartID(), itemidview, 5);

	}
	

	@Test(expected = InvalidQuantity_Exception.class)
	public void addToCartInvalidQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("cartId", itemidview2, -5);
	}
	
	@Test(expected = InvalidQuantity_Exception.class)
	public void addToCartZeroQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("cartId", itemidview2, 0);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartNullTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(null, null, 0);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartEmptyNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("", null, 0);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartWhiteSpaceNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(" ", null, 5);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartTabNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("\t", null, 5);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartNewLineNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("\n", null, 5);
	}
	
	
	
	
	
	
	
	/////////////////////////// BAD ANGELA BAD ///////////////////////////////
	public void addCartBadCartName() {
		
	
	}
	
	@Test
	public void addCartBadProdId() {
		
	
	}
	
	@Test
	public void addCartOtherBadProdId() {
		
	
	}
	
	@Test
	public void addCartBadQuantity() {
		
	
	}
	
	@Test
	public void addCartZeroQuantity() {
		
	
	}
	
	@Test
	public void addCartExistentCartSuccess() {
		
	
	}
	
	@Test
	public void addCartNewCartSuccess() {
		
	
	}
	
	@Test
	public void addCartTwoProductsSuccess() {
		
	
	}
	
	@Test
	public void addCartSameProductTwiceSuccess() {
		
	
	}
	
	@Test
	public void addCartSameProductTwiceNotEnoughInStockFailure() {
		
	
	}
}
	
	
	