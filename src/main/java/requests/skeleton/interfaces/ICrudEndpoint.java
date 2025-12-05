package requests.skeleton.interfaces;

import models.BaseModel;

public interface ICrudEndpoint {
    Object post(BaseModel model);
    Object get();
    Object put(BaseModel model);
    Object delete(int id);
}
