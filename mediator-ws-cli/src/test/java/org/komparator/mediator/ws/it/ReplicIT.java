package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.Result;
import org.komparator.mediator.ws.ShoppingResultView;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;

public class ReplicIT extends BaseIT {
	private static final String VALID_CC = "1234567890123452";

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		// clear remote service state before each test
		mediatorClient.clear();

		// fill-in test products
		// (since buyProduct is a read/write operation
		// the initialization below is done for each test)
		{
			ProductView prod = new ProductView();
			prod.setId("p1");
			prod.setDesc("AAA bateries (pack of 3)");
			prod.setPrice(3);
			prod.setQuantity(10);
			supplierClients[0].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p1");
			prod.setDesc("3batteries");
			prod.setPrice(4);
			prod.setQuantity(10);
			supplierClients[1].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p2");
			prod.setDesc("AAA bateries (pack of 10)");
			prod.setPrice(9);
			prod.setQuantity(20);
			supplierClients[0].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p2");
			prod.setDesc("10x AAA battery");
			prod.setPrice(8);
			prod.setQuantity(20);
			supplierClients[1].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p3");
			prod.setDesc("Digital Multimeter");
			prod.setPrice(15);
			prod.setQuantity(5);
			supplierClients[0].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p4");
			prod.setDesc("very cheap batteries");
			prod.setPrice(2);
			prod.setQuantity(5);
			supplierClients[0].createProduct(prod);
		}
	}

	@After
	public void tearDown() {
		// clear remote service state after each test
		mediatorClient.clear();
		// even though mediator clear should have cleared suppliers, clear them
		// explicitly after use
		supplierClients[0].clear();
		supplierClients[1].clear();
	}

	

	

	@Test
	public void testReplic() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products to carts --
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 2);
		}

		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[1]);
			mediatorClient.addToCart("xyz", id, 1);
		}

		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 3);
		}

		{ // product in other cart! (will not try to buy this)
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[1]);
			mediatorClient.addToCart("DoNotBuyMe", id, 1);
		}

		MediatorClient med2 = null;
		try {
			System.out.println("Please kill the Primary Mediator and wait a few seconds.");
			TimeUnit.SECONDS.sleep(80);
			med2 = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
		} catch (InterruptedException | MediatorClientException e) {
			System.err.println("Caught exception:" + e);
		}
		
		assertNotNull(med2);
		
		ShoppingResultView shpResView = med2.buyCart("xyz", VALID_CC);
		assertNotNull(shpResView.getId());
		assertEquals(Result.COMPLETE, shpResView.getResult());
		assertEquals(0, shpResView.getDroppedItems().size());
		assertEquals(3, shpResView.getPurchasedItems().size());
		final int expectedTotalPrice = 2 * 3 + 1 * 4 + 3 * 9; // sum(qty*price)
		assertEquals(expectedTotalPrice, shpResView.getTotalPrice());
	}
}
