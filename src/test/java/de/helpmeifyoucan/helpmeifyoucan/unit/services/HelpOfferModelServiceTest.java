package de.helpmeifyoucan.helpmeifyoucan.unit.services;

import de.helpmeifyoucan.helpmeifyoucan.StaticDbClear;
import de.helpmeifyoucan.helpmeifyoucan.models.Coordinates;
import de.helpmeifyoucan.helpmeifyoucan.models.HelpModelApplication;
import de.helpmeifyoucan.helpmeifyoucan.models.HelpOfferModel;
import de.helpmeifyoucan.helpmeifyoucan.models.UserModel;
import de.helpmeifyoucan.helpmeifyoucan.models.dtos.request.HelpOfferUpdate;
import de.helpmeifyoucan.helpmeifyoucan.services.HelpOfferModelService;
import de.helpmeifyoucan.helpmeifyoucan.utils.HelpCategoryEnum;
import de.helpmeifyoucan.helpmeifyoucan.utils.PostStatusEnum;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class HelpOfferModelServiceTest {

    @Autowired
    private StaticDbClear clear;

    @Autowired
    private HelpOfferModelService helpService;

    private HelpOfferModel testOffer;

    private UserModel testUser;

    private Coordinates testCoordinates;

    @Before
    public void setUpTest() {
        testCoordinates = new Coordinates().setLatitude(50.00).setLongitude(20.00);
        testUser = new UserModel().setLastName("og").setName("tripple").setEmail("test@mail.de");
        testUser.setId(new ObjectId());
        testOffer = new HelpOfferModel().setUser(testUser.getId()).setCoordinates(testCoordinates).addCategory(HelpCategoryEnum.PersonalAssistance).setStatus(PostStatusEnum.ACTIVE).setDescription("Delphine sind schwule haie");
        clear.clearDb();
    }

    @Test
    public void givenOfferToSave_offerShouldBeSavedAndBeRetrieved() {
        this.helpService.saveNewOffer(testOffer, testUser.getId());

        assertEquals(testOffer, this.helpService.getById(testOffer.getId()));
    }

    @Test
    public void givenOfferAndUpdateToPerform_offerShouldBeUpdatedAccordingly() {
        this.helpService.saveNewOffer(testOffer, testUser.getId());


        List<HelpCategoryEnum> newCategories = new LinkedList<>();
        newCategories.add(HelpCategoryEnum.Errands);
        newCategories.add(HelpCategoryEnum.PersonalAssistance);
        HelpOfferUpdate offerUpdate = new HelpOfferUpdate().setStatus(PostStatusEnum.INACTIVE).setDescription("updatedDescription").setCategories(newCategories);

        var updatedOffer = this.helpService.update(testOffer.getId(), offerUpdate, testUser.getId());

        assertEquals(updatedOffer.getStatus(), PostStatusEnum.INACTIVE);
        assertEquals(updatedOffer.getCategories(), newCategories);
        assertEquals(updatedOffer.getDescription(), offerUpdate.description);
    }

    @Test
    public void givenTwoObjectsMatchingTheFilter_updateManyShouldUpdateTwo() {
        this.helpService.saveNewOffer(testOffer, testUser.getId());

        var newOffer = new HelpOfferModel().setUser(new ObjectId()).setCoordinates(testCoordinates).addCategory(HelpCategoryEnum.PersonalAssistance).setStatus(PostStatusEnum.ACTIVE).setDescription("Delphine sind schwule haie");

        this.helpService.saveNewOffer(newOffer, newOffer.getUser());

        long updatedOffers = this.helpService.updateEmbeddedCoordinates(testCoordinates.setLatitude(25.00));

        assertEquals(updatedOffers, 2);
    }


    @Test
    public void givenApplicationsToSave_itShouldBeSaved() {
        this.helpService.saveNewOffer(testOffer, testUser.getId());

        var testApplication = new HelpModelApplication().setUser(testUser).setMessage("testmessage").setTelephoneNr("01231548135");

        this.helpService.saveNewApplication(testOffer.getId(), testApplication);

        var updateOffer = this.helpService.getById(testOffer.getId());

        assertEquals(updateOffer.getApplications().get(0), testApplication);

        assertTrue(updateOffer.getApplications().contains(testApplication));
    }

    @Test
    public void givenApplicationToDelete_itShouldBeRemoved() {
        this.helpService.saveNewOffer(testOffer, testUser.getId());

        var testApplication = new HelpModelApplication().setUser(testUser).setMessage("testmessage").setTelephoneNr("01231548135");

        this.helpService.saveNewApplication(testOffer.getId(), testApplication);

        this.helpService.deleteApplication(testOffer.getId(), testApplication.getId(), testUser.getId());

        var updatedOffer = this.helpService.getById(testOffer.getId());
        assertFalse(updatedOffer.getApplications().contains(testApplication));
    }

    @Test
    public void givenApplicationToAccept_itShouldBeAcceptedCorrectly() {
        this.helpService.saveNewOffer(testOffer, testUser.getId());

        var testApplication = new HelpModelApplication().setUser(testUser).setMessage("testmessage").setTelephoneNr("01231548135");

        this.helpService.saveNewApplication(testOffer.getId(), testApplication);

        this.helpService.acceptApplication(testOffer.getId(), testApplication.getId(), testUser.getId());
    }


}