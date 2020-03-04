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
        @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
        if @user
            BlacklistedToken.create(token: http_auth_header, user: @user, expire_at: decoded_auth_token[:exp]) if decoded_auth_token
        else
            errors.add(:token, 'User not found')
        end
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