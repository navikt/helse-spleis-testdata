type Headers = { [key: string]: string };

const baseUrl =
  import.meta.env.MODE === "development" ? "http://0.0.0.0:8080" : "";

export const post = (
  path: string,
  body?: any,
  headers?: Headers
): Promise<Response> => {
  return fetch(`${baseUrl}${path}`, {
    method: "post",
    body: JSON.stringify(body),
    headers: { "Content-Type": "application/json", ...headers },
  });
};

export const get = (path: string, headers?: Headers): Promise<Response> => {
  return fetch(`${baseUrl}${path}`, {
    method: "get",
    headers: {
      Accept: "application/json",
      ...headers,
    },
  });
};

export const del = (path: string, headers?: Headers): Promise<Response> => {
  return fetch(`${baseUrl}${path}`, {
    method: "delete",
    headers,
  });
};
