class BlacklistToken
    prepend SimpleCommand

    def initialize(headers = {})
        @headers = headers
    end

    def call
        blacklist
    end

    private

    attr_accessor :headers

    def blacklist
        check = AuthorizeApiRequest.call(headers)
        BlacklistedToken.create(token: http_auth_header, user_id: check.result, expire_at: decoded_auth_token[:exp]) if check.success?
    end

    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken::decode(http_auth_header)
    end
  
    def http_auth_header

        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end
end