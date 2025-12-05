package api.requests.skeleton.interfaces;

import api.models.BaseModel;

public interface ICrudEndpoint {
    Object post(BaseModel model);
    Object get();
    Object put(BaseModel model);
    Object delete(int id);
}
