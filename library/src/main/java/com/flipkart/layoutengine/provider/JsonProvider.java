package com.flipkart.layoutengine.provider;

import com.flipkart.layoutengine.exceptions.InvalidDataPathException;
import com.flipkart.layoutengine.exceptions.JsonNullException;
import com.flipkart.layoutengine.exceptions.NoSuchDataPathException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.StringTokenizer;

/**
 * Created by kirankumar on 24/06/14.
 */
public class JsonProvider implements Provider {
    private JsonElement rootElement;

    public JsonProvider(JsonElement jsonElement) {
        this.rootElement = jsonElement;
    }

    @Override
    public void setData(JsonElement rootElement) {
        this.rootElement = rootElement;
    }

    @Override
    public JsonElement getData() {
        return this.rootElement;
    }

    @Override
    public JsonElement getObject(String key, int childIndex)
            throws InvalidDataPathException, NoSuchDataPathException, JsonNullException {
        return getFromObject(key, childIndex);
    }

    private JsonElement getFromObject(String path, int childIndex)
            throws InvalidDataPathException, JsonNullException, NoSuchDataPathException {
        JsonElement root = this.rootElement;
        StringTokenizer tokenizer = new StringTokenizer(path, ProteusConstants.DATA_PATH_DELIMITERS);
        JsonElement elementToReturn = root;
        JsonElement tempElement;
        JsonArray tempArray;

        while (tokenizer.hasMoreTokens()) {
            String segment = tokenizer.nextToken();
            if (elementToReturn == null) {
                throw new NoSuchDataPathException(path);
            }
            if (elementToReturn.isJsonNull()) {
                throw new JsonNullException(path);
            }
            if ("".equals(segment)) {
                continue;
            }
            if (elementToReturn.isJsonArray()) {
                tempArray = elementToReturn.getAsJsonArray();
                if (tempArray != null) {
                    if (ProteusConstants.CHILD_INDEX_REFERENCE.equals(segment)) {
                        if (childIndex < tempArray.size()) {
                            elementToReturn = tempArray.get(childIndex);
                        } else {
                            throw new NoSuchDataPathException(path + "@[" + childIndex + "]");
                        }
                    } else if (ProteusConstants.ARRAY_DATA_LENGTH_REFERENCE.equals(segment)) {
                        elementToReturn = new JsonPrimitive(tempArray.size());
                    } else if (ProteusConstants.ARRAY_DATA_LAST_INDEX_REFERENCE.equals(segment)) {
                        if (tempArray.size() == 0) {
                            throw new NoSuchDataPathException(path + "@[" + segment + "]");
                        }
                        elementToReturn = tempArray.get(tempArray.size() - 1);
                    } else {
                        int index;
                        try {
                            index = Integer.parseInt(segment);
                        } catch (NumberFormatException e) {
                            throw new InvalidDataPathException(path + "@[" + segment + "]");
                        }
                        if (index < tempArray.size()) {
                            elementToReturn = tempArray.get(index);
                        } else {
                            throw new NoSuchDataPathException(path + "@[" + index + "]");
                        }
                    }
                } else {
                    throw new NoSuchDataPathException(path);
                }
            } else if (elementToReturn.isJsonObject()) {
                tempElement = elementToReturn.getAsJsonObject().get(segment);
                if (tempElement != null) {
                    elementToReturn = tempElement;
                } else {
                    throw new NoSuchDataPathException(path);
                }
            } else if (elementToReturn.isJsonPrimitive()) {
                throw new InvalidDataPathException(path);
            } else {
                throw new NoSuchDataPathException(path);
            }
        }
        if (elementToReturn.isJsonNull()) {
            throw new JsonNullException(path);
        }
        return elementToReturn;
    }

    @Override
    public Provider clone() {
        try {
            return (Provider) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
