package csw.scylladb.jwt.kotlintest.service

import csw.scylladb.jwt.kotlintest.dto.OAuthAttributes
import csw.scylladb.jwt.kotlintest.model.CustomOAuth2User
import csw.scylladb.jwt.kotlintest.model.User
import csw.scylladb.jwt.kotlintest.repository.UserRepository
import org.springframework.core.env.Environment
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val env: Environment
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): CustomOAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val userNameAttributeName =
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        val attributes: OAuthAttributes =
            OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.attributes, env)
        val user: User = saveOrUpdate(attributes)

        //
//        httpSession.setAttribute("user", new SessionUser(user));

//        return new DefaultOAuth2User(user.getAuthorities(), attributes.getAttributes(), attributes.getNameAttributeKey());
        return CustomOAuth2User(user, oAuth2User.attributes)
    }

    private fun saveOrUpdate(attributes: OAuthAttributes): User {
        val user: User = userRepository.findByProviderAndEmail(attributes.provider, attributes.email)
            ?.update(attributes.name, attributes.picture)
            ?: attributes.toEntity()

        return userRepository.save(user)
    }
}