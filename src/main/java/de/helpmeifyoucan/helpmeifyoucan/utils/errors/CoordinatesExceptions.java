package de.helpmeifyoucan.helpmeifyoucan.utils.errors;

import org.bson.types.ObjectId;

import de.helpmeifyoucan.helpmeifyoucan.utils.errors.AbstractExceptions.NotFoundException;

public final class CoordinatesExceptions {

    public static class CoordinatesNotFoundException extends NotFoundException {
        private static final long serialVersionUID = 7415040101907590277L;
        private static ErrorCode error = ErrorCode.COORDINATES_NOT_FOUND;


        public CoordinatesNotFoundException(ObjectId id) {
            this(id.toString());

        }

        public CoordinatesNotFoundException(String coordinates) {
            super(String.format(error.getMessage(), coordinates), error.getCode());
        }
    }


}
