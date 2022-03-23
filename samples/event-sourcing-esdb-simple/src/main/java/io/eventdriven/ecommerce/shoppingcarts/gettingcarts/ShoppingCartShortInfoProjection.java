package io.eventdriven.ecommerce.shoppingcarts.gettingcarts;

import io.eventdriven.ecommerce.core.events.EventEnvelope;
import io.eventdriven.ecommerce.core.projections.JPAProjection;
import io.eventdriven.ecommerce.shoppingcarts.Events;
import io.eventdriven.ecommerce.shoppingcarts.ShoppingCart;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShoppingCartShortInfoProjection extends JPAProjection<ShoppingCartShortInfo, UUID> {
  protected ShoppingCartShortInfoProjection(ShoppingCartShortInfoRepository repository) {
    super(repository);
  }

  @EventListener
  public void handleShoppingCartOpened(EventEnvelope<Events.ShoppingCartOpened> eventEnvelope) {
    add(eventEnvelope, () ->
      new ShoppingCartShortInfo(
        eventEnvelope.data().shoppingCartId(),
        eventEnvelope.data().clientId(),
        ShoppingCart.Status.Pending,
        0,
        0,
        eventEnvelope.metadata().streamPosition(),
        eventEnvelope.metadata().logPosition()
      )
    );
  }

  @EventListener
  public void handleProductItemAddedToShoppingCart(EventEnvelope<Events.ProductItemAddedToShoppingCart> eventEnvelope) {
    getAndUpdate(eventEnvelope.data().shoppingCartId(), eventEnvelope,
      view -> view.increaseProducts(eventEnvelope.data().productItem())
    );
  }

  @EventListener
  public void handleProductItemRemovedFromShoppingCart(EventEnvelope<Events.ProductItemRemovedFromShoppingCart> eventEnvelope) {
    getAndUpdate(eventEnvelope.data().shoppingCartId(), eventEnvelope,
      view -> view.decreaseProducts(eventEnvelope.data().productItem())
    );
  }

  @EventListener
  public void handleShoppingCartConfirmed(EventEnvelope<Events.ShoppingCartConfirmed> eventEnvelope) {
    getAndUpdate(eventEnvelope.data().shoppingCartId(), eventEnvelope,
      view -> view.setStatus(ShoppingCart.Status.Confirmed)
    );
  }

  @EventListener
  public void handleShoppingCartCanceled(EventEnvelope<Events.ShoppingCartCanceled> eventEnvelope) {
    DeleteById(eventEnvelope.data().shoppingCartId());
  }
}
