package de.helpmeifyoucan.helpmeifyoucan.services;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import de.helpmeifyoucan.helpmeifyoucan.models.Coordinates;
import de.helpmeifyoucan.helpmeifyoucan.models.HelpRequestModel;
import de.helpmeifyoucan.helpmeifyoucan.models.dtos.request.CoordinatesUpdate;
import de.helpmeifyoucan.helpmeifyoucan.models.dtos.request.HelpRequestUpdate;
import de.helpmeifyoucan.helpmeifyoucan.utils.errors.HelpRequestModelExceptions;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;

@Service
public class HelpRequestModelService extends AbstractService<HelpRequestModel> {


    private CoordinatesService coordinatesService;

    @Autowired
    public HelpRequestModelService(MongoDatabase dataBase, CoordinatesService coordinatesService) {
        super(dataBase);
        super.createCollection("helpRequests", HelpRequestModel.class);
        this.createIndex();
        this.coordinatesService = coordinatesService;
    }

    private void createIndex() {
        IndexOptions options = new IndexOptions();
        options.unique(true);

        super.createIndex(Indexes.ascending("datePublished", "user"), options);
    }


    /**
     * We want to update a request, we do so by first checking if the updating user has permission to do so, if yes we directly update the request in the db
     *
     * @param requestToUpdate the request to update
     * @param update          the update to perform
     * @param updatingUser    the user performing the updating
     * @return the updated request
     */

    public HelpRequestModel update(ObjectId requestToUpdate, HelpRequestUpdate update, ObjectId updatingUser) {

        var updateFilter = and(eq(requestToUpdate), in("user", updatingUser));

        return Optional.ofNullable(super.updateExistingFields(updateFilter, update.toFilter())).orElseThrow(() -> new HelpRequestModelExceptions.HelpRequestNotFoundException(requestToUpdate));

    }


    /**
     * We want to save a new helpRequest, so we first generate an id, to then save the embedded coordinates and finally save the request with a added ref to the added coordinates
     *
     * @param request the request to update
     * @param user    the saving user
     * @return the saved request
     */
    public HelpRequestModel saveNewRequest(HelpRequestModel request, ObjectId user) {

        request.generateId();

        var addedCords = this.coordinatesService.handleHelpModelCoordinateAdd(request);

        this.updateEmbeddedCoordinates(addedCords);

        return this.save(request.setCoordinates(addedCords));
    }

    /**
     * We want to update the embedded coordinates a request has, so we first check if the updating user matches the user registered in the request ref and then delegate the updating
     * to the embedded coordinates controller. to then update the db request db model
     *
     * @param requestId    the request to update
     * @param update       the update to perform
     * @param updatingUser the user performing the update
     * @return the updated request
     */

    public HelpRequestModel handleCoordinatesUpdate(ObjectId requestId, CoordinatesUpdate update, ObjectId updatingUser) {
        var requestToUpdate = getByRequestIdAndUser(requestId, updatingUser);

        var updatedCoordinates = this.coordinatesService.handleHelpModelCoordinateUpdate(requestToUpdate, update);

        this.updateEmbeddedCoordinates(updatedCoordinates);

        return requestToUpdate.setCoordinates(updatedCoordinates);
    }

    /**
     * We want to delete a request, we do so by first checking if the user is permitted to do so by checking the request user ref,
     * if permitted, the request will be deleted and the deletion of its embedded coordinates will be handled by the embedded coordiantes controller
     * the coordinates returned by this controller will then be updated in the request tables model refs
     *
     * @param requestToDelete the request to delete
     * @param deletingUser    the user performing the delete
     * @return the deleted request
     */

    public HelpRequestModel deleteRequest(ObjectId requestToDelete, ObjectId deletingUser) {

        var deleteFilter = and(eq(requestToDelete), in("user", deletingUser));

        var deletedOffer = this.findOneAndDelete(deleteFilter);

        var deletedCoords = this.coordinatesService.handleHelpModelCoordinateDelete(deletedOffer);

        this.updateEmbeddedCoordinates(deletedCoords);

        return deletedOffer;

    }

    /**
     * We want the data integrity to be intact, so once we register an coordinates update, we will update the models holding refs to it
     *
     * @param updatedCoords the updated coords
     * @return number of objects updated
     */

    public long updateEmbeddedCoordinates(Coordinates updatedCoords) {
        var filter = in("coordinates._id", updatedCoords.getId());

        var update = set("coordinates", updatedCoords);

        return this.updateMany(filter, update).getModifiedCount();
    }

    private HelpRequestModel getByRequestIdAndUser(ObjectId requestId, ObjectId userId) {

        return Optional.ofNullable(getByFilter(and(eq(requestId), in("user", userId)))).orElseThrow(() -> new HelpRequestModelExceptions.HelpRequestNotFoundException(requestId));

    }


}
