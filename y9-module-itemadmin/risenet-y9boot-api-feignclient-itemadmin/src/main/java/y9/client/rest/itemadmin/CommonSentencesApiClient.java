package y9.client.rest.itemadmin;

import org.springframework.cloud.openfeign.FeignClient;

import net.risesoft.api.itemadmin.CommonSentencesApi;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
@FeignClient(contextId = "CommonSentencesApiClient", name = "${y9.service.itemAdmin.name:itemAdmin}",
    url = "${y9.service.itemAdmin.directUrl:}",
    path = "/${y9.service.itemAdmin.name:itemAdmin}/services/rest/commonSentences")
public interface CommonSentencesApiClient extends CommonSentencesApi {

}
