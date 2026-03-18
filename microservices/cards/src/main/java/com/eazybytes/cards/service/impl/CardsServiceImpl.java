package com.eazybytes.cards.service.impl;

import com.eazybytes.cards.constants.CardsConstants;
import com.eazybytes.cards.dto.CardsDto;
import com.eazybytes.cards.entity.Cards;
import com.eazybytes.cards.exception.CardAlreadyExistsException;
import com.eazybytes.cards.exception.ResourceNotFoundException;
import com.eazybytes.cards.mapper.CardsMapper;
import com.eazybytes.cards.repository.CardsRepository;
import com.eazybytes.cards.service.ICardsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class CardsServiceImpl implements ICardsService {

    private CardsRepository cardsRepository;

    /**
     * @param mobileNumber - Mobile Number of the Customer
     */
    @Override
    public void createCard(String mobileNumber) {
        log.debug("Logic: CreateCard | Checking if card exists for mobile: {}", mobileNumber);

        Optional<Cards> optionalCards = cardsRepository.findByMobileNumber(mobileNumber);
        if(optionalCards.isPresent()){
            log.warn("Logic: CreateCard | Failure: Card already exists | Mobile: {}", mobileNumber);
            throw new CardAlreadyExistsException("Card already registered with given mobileNumber " + mobileNumber);
        }

        Cards newCard = createNewCard(mobileNumber);
        cardsRepository.save(newCard);
        log.info("Logic: CreateCard | Success: Card {} generated for Mobile: {}", newCard.getCardNumber(), mobileNumber);
    }

    /**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new card details
     */
    private Cards createNewCard(String mobileNumber) {
        log.trace("Logic: createNewAccount | Assigning new card number and limit for Mobile: {}", mobileNumber);
        Cards newCard = new Cards();
        long randomCardNumber = 100000000000L + new Random().nextInt(900000000);
        newCard.setCardNumber(Long.toString(randomCardNumber));
        newCard.setMobileNumber(mobileNumber);
        newCard.setCardType(CardsConstants.CREDIT_CARD);
        newCard.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        newCard.setAmountUsed(0);
        newCard.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
        return newCard;
    }

    /**
     *
     * @param mobileNumber - Input mobile Number
     * @return Card Details based on a given mobileNumber
     */
    @Override
    public CardsDto fetchCard(String mobileNumber) {
        log.debug("Logic: FetchCard | Querying DB for Mobile: {}", mobileNumber);

        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> {
                    log.warn("Logic: FetchCard | NotFound: Card | Mobile: {}", mobileNumber);
                    return new ResourceNotFoundException("Card", "mobileNumber", mobileNumber);
                }
        );
        return CardsMapper.mapToCardsDto(cards, new CardsDto());
    }

    /**
     *
     * @param cardsDto - CardsDto Object
     * @return boolean indicating if the update of card details is successful or not
     */
    @Override
    public boolean updateCard(CardsDto cardsDto) {
        log.debug("Logic: UpdateCard | Updating details for CardNo: {}", cardsDto.getCardNumber());

        Cards cards = cardsRepository.findByCardNumber(cardsDto.getCardNumber()).orElseThrow(
                () -> {
                    log.error("Logic: UpdateCard | NotFound: CardNo: {}", cardsDto.getCardNumber());
                    return new ResourceNotFoundException("Card", "CardNumber", cardsDto.getCardNumber());
                }
        );

        CardsMapper.mapToCards(cardsDto, cards);
        cardsRepository.save(cards);

        log.info("Logic: UpdateCard | Success: CardNo: {} updated", cards.getCardNumber());
        return true;
    }

    /**
     * @param mobileNumber - Input MobileNumber
     * @return boolean indicating if the delete of card details is successful or not
     */
    @Override
    public boolean deleteCard(String mobileNumber) {
        log.debug("Logic: DeleteCard | Attempting deletion for Mobile: {}", mobileNumber);

        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );

        cardsRepository.deleteById(cards.getCardId());
        log.info("Logic: DeleteCard | Success: Deleted Card ID: {} for Mobile: {}", cards.getCardId(), mobileNumber);
        return true;
    }


}
