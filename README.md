# Opening Hours Renderer API
HTTP API that accepts opening hours data as an input (JSON) and returns a more human readable version of the data formatted using a 12-hour clock.

Input example:
```json
{
  "monday":[],
  "tuesday":[
    {
      "type":"open",
      "value":36000
    },
    {
      "type":"close",
      "value":64800
    }
  ],
  "wednesday":[],
  "thursday":[
    {
      "type":"open",
      "value":37800
    },
    {
      "type":"close",
      "value":64800
    }
  ],
  "friday":[
    {
      "type":"open",
      "value":36000
    }
  ],
  "saturday":[
    {
      "type":"close",
      "value":3600
    },
    {
      "type":"open",
      "value":36000
    }
  ],
  "sunday":[
    {
      "type":"close",
      "value":3600
    },
    {
      "type":"open",
      "value":43200
    },
    {
      "type":"close",
      "value":75600
    }
  ]
}
```


Output example in 12-hour clock format:
```
Monday: 8 AM - 10 AM, 11 AM - 6 PM
Tuesday: Closed
Wednesday: 11 AM - 6 PM
Thursday: 11 AM - 6 PM
Friday: 11 AM - 9 PM
Saturday: 11 AM - 9 PM
Sunday: Closed
```

# Requisites
- JDK 11+
- Apache Maven 3.6+

# Build & Run 
On repository root directory, execute:
- Windows
```batch
mvnw.cmd spring-boot:run
```
- MacOs/Unix
```bash
./mvnw spring-boot:run
```

The application server should start on port `8080`

# Test
To test, simply post a request (using `cURL`, for example) to `/opening-hours` endpoint with the input JSON on request body

For example:
```bash
curl -s -X POST http://localhost:8080/opening-hours \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d "{\"monday\":[],\"tuesday\":[{\"type\":\"open\",\"value\":36000},{\"type\":\"close\",\"value\":64800}],\"wednesday\":[],\"thursday\":[{\"type\":\"open\",\"value\":37800},{\"type\":\"close\",\"value\":64800}],\"friday\":[{\"type\":\"open\",\"value\":36000}],\"saturday\":[{\"type\":\"close\",\"value\":3600},{\"type\":\"open\",\"value\":36000}],\"sunday\":[{\"type\":\"close\",\"value\":3600},{\"type\":\"open\",\"value\":43200},{\"type\":\"close\",\"value\":75600}]}" 
```

---
# Considerations (Part 2 Assignment)

IMHO, the current format/schema of opening hours JSON is not ideal:
* No common pattern regarding properties: day of week is not a  named-property while open/close hour event type is.
* To be less error-prone, the opening hours periods could be informed self-contained instead of separate events (open/close). What would make the parsing/processing way easier also
* The time field could be informed using a string timestamp (ISO-8601, for example) with timezone information included (or UTC fixed) to avoid any locale and/or zoned parsing mistake (could use epoch date as base date)
* For closed days, not only using an empty list to represent it but also the lack of information for a specific day could represent that on that day the restaurant is closed. This way we could always have a full week schedule
 